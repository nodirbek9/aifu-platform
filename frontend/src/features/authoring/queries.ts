import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query'
import type { components } from '@/shared/api/schema'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'
import type { SessionType } from '@/shared/ui/badge'
import {
  narrowAuthoringCourse,
  narrowBlock,
  narrowEditorLesson,
  narrowModule,
  type BlockType,
  type BlockVisibility,
  type EditorLesson,
  type EditorModule,
  type JsonObject,
  type PublicationStatus,
} from './model'

type Schemas = components['schemas']

export const authoringKeys = {
  course: (courseId: string) => ['authoring', 'course', courseId] as const,
  tree: (courseId: string) => ['authoring', 'tree', courseId] as const,
  lesson: (lessonId: string) => ['authoring', 'lesson', lessonId] as const,
}

export type CurriculumTree = { modules: (EditorModule & { lessons: EditorLesson[] })[] }

/** The course as its editor needs it: required languages, hour plan, assessment scheme. */
export function useAuthoringCourseQuery(courseId: string) {
  return useQuery({
    queryKey: authoringKeys.course(courseId),
    queryFn: async () =>
      narrowAuthoringCourse(
        await unwrap(api.GET('/api/v1/teacher/courses/{id}', { params: { path: { id: courseId } } })),
      ),
  })
}

/** Every module of the course with its lessons, any status, in order. */
export function useCurriculumTreeQuery(courseId: string) {
  return useQuery({
    queryKey: authoringKeys.tree(courseId),
    queryFn: async (): Promise<CurriculumTree> => {
      const page = await unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/modules', {
          params: { path: { courseId }, query: { page: 0, size: 200 } },
        }),
      )
      const modules = (page.content ?? [])
        .map(narrowModule)
        .filter((module) => module !== null)
        .sort((a, b) => a.position - b.position)
      const lessons = await Promise.all(
        modules.map(async (module) => {
          const lessonPage = await unwrap(
            api.GET('/api/v1/teacher/modules/{moduleId}/lessons', {
              params: { path: { moduleId: module.id }, query: { page: 0, size: 200 } },
            }),
          )
          return (lessonPage.content ?? [])
            .map(narrowEditorLesson)
            .filter((lesson) => lesson !== null)
            .sort((a, b) => a.position - b.position)
        }),
      )
      return { modules: modules.map((module, index) => ({ ...module, lessons: lessons[index] ?? [] })) }
    },
  })
}

function useTreeMutation<V>(courseId: string, mutationFn: (variables: V) => Promise<unknown>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSettled: () => {
      void queryClient.invalidateQueries({ queryKey: authoringKeys.tree(courseId) })
      void queryClient.invalidateQueries({ queryKey: ['teacher', 'course-lessons', courseId] })
    },
  })
}

export function useCreateModule(courseId: string) {
  return useTreeMutation(
    courseId,
    async ({ code, title, locale }: { code: string; title: string; locale: Locale }) => {
      const module = await unwrap(
        api.POST('/api/v1/teacher/courses/{courseId}/modules', {
          params: { path: { courseId } },
          body: { code, groupingSource: 'TEACHER_DEFINED' },
        }),
      )
      if (module.id === undefined) return
      await unwrap(
        api.PUT('/api/v1/teacher/modules/{id}/translations/{locale}', {
          params: { path: { id: module.id, locale } },
          body: { title },
        }),
      )
    },
  )
}

export function useRenameModule(courseId: string) {
  return useTreeMutation(
    courseId,
    ({
      id,
      locale,
      title,
      description,
    }: {
      id: string
      locale: Locale
      title: string
      description?: string
    }) =>
      unwrap(
        api.PUT('/api/v1/teacher/modules/{id}/translations/{locale}', {
          params: { path: { id, locale } },
          body: { title, ...(description !== undefined && { description }) },
        }),
      ),
  )
}

export function useModuleStatus(courseId: string) {
  return useTreeMutation(courseId, ({ id, status }: { id: string; status: PublicationStatus }) =>
    unwrap(api.PUT('/api/v1/teacher/modules/{id}/status', { params: { path: { id } }, body: { status } })),
  )
}

export function useDeleteModule(courseId: string) {
  return useTreeMutation(courseId, (id: string) =>
    unwrap(api.DELETE('/api/v1/teacher/modules/{id}', { params: { path: { id } } })),
  )
}

/** Reorders optimistically: the tree shows the new order at once and goes back if the server says no. */
export function useReorderModules(courseId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (orderedModuleIds: string[]) =>
      unwrap(
        api.PUT('/api/v1/teacher/courses/{courseId}/modules/reorder', {
          params: { path: { courseId } },
          body: { orderedModuleIds },
        }),
      ),
    onMutate: (ordered) =>
      patchTree(queryClient, courseId, (tree) => ({
        modules: ordered.flatMap((id) => tree.modules.filter((module) => module.id === id)),
      })),
    onError: (_error, _ordered, previous) => {
      if (previous !== undefined) queryClient.setQueryData(authoringKeys.tree(courseId), previous)
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: authoringKeys.tree(courseId) }),
  })
}

export function useReorderLessons(courseId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ moduleId, orderedLessonIds }: { moduleId: string; orderedLessonIds: string[] }) =>
      unwrap(
        api.PUT('/api/v1/teacher/modules/{moduleId}/lessons/reorder', {
          params: { path: { moduleId } },
          body: { orderedLessonIds },
        }),
      ),
    onMutate: ({ moduleId, orderedLessonIds }) =>
      patchTree(queryClient, courseId, (tree) => ({
        modules: tree.modules.map((module) =>
          module.id !== moduleId
            ? module
            : {
                ...module,
                lessons: orderedLessonIds.flatMap((id) =>
                  module.lessons.filter((lesson) => lesson.id === id),
                ),
              },
        ),
      })),
    onError: (_error, _variables, previous) => {
      if (previous !== undefined) queryClient.setQueryData(authoringKeys.tree(courseId), previous)
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: authoringKeys.tree(courseId) }),
  })
}

async function patchTree(
  queryClient: QueryClient,
  courseId: string,
  change: (tree: CurriculumTree) => CurriculumTree,
): Promise<CurriculumTree | undefined> {
  const key = authoringKeys.tree(courseId)
  await queryClient.cancelQueries({ queryKey: key })
  const previous = queryClient.getQueryData<CurriculumTree>(key)
  if (previous !== undefined) queryClient.setQueryData(key, change(previous))
  return previous
}

export type NewLesson = {
  moduleId: string
  code: string
  sessionType: SessionType
  hours?: number
  title: string
  locale: Locale
}

export function useCreateLesson(courseId: string) {
  return useTreeMutation(courseId, async (lesson: NewLesson): Promise<string | undefined> => {
    const created = await unwrap(
      api.POST('/api/v1/teacher/modules/{moduleId}/lessons', {
        params: { path: { moduleId: lesson.moduleId } },
        body: { code: lesson.code, sessionType: lesson.sessionType },
      }),
    )
    if (created.id === undefined) return undefined
    if (lesson.hours !== undefined) {
      await unwrap(
        api.PATCH('/api/v1/teacher/lessons/{id}', {
          params: { path: { id: created.id } },
          body: { sessionType: lesson.sessionType, allocatedHours: lesson.hours },
        }),
      )
    }
    await unwrap(
      api.PUT('/api/v1/teacher/lessons/{id}/translations/{locale}', {
        params: { path: { id: created.id, locale: lesson.locale } },
        body: { title: lesson.title },
      }),
    )
    return created.id
  })
}

export function useLessonStatus(courseId: string) {
  return useTreeMutation(courseId, ({ id, status }: { id: string; status: PublicationStatus }) =>
    unwrap(api.PUT('/api/v1/teacher/lessons/{id}/status', { params: { path: { id } }, body: { status } })),
  )
}

export function useDeleteLesson(courseId: string) {
  return useTreeMutation(courseId, (id: string) =>
    unwrap(api.DELETE('/api/v1/teacher/lessons/{id}', { params: { path: { id } } })),
  )
}

// ------------------------------------------------------------------ one lesson

export function useEditorLessonQuery(lessonId: string) {
  return useQuery({
    queryKey: authoringKeys.lesson(lessonId),
    queryFn: async () => {
      const lesson = narrowEditorLesson(
        await unwrap(api.GET('/api/v1/teacher/lessons/{id}', { params: { path: { id: lessonId } } })),
      )
      if (lesson === null) throw new Error('Lesson without id')
      return lesson
    },
  })
}

function useLessonCache(lessonId: string) {
  const queryClient = useQueryClient()
  const key = authoringKeys.lesson(lessonId)
  return {
    queryClient,
    /** The lesson as the server just returned it replaces the cached one. */
    setLesson: (raw: Schemas['LessonDetail']) => {
      const lesson = narrowEditorLesson(raw)
      if (lesson !== null) queryClient.setQueryData(key, lesson)
    },
    /** One block as the server returned it replaces the cached block. */
    setBlock: (raw: Schemas['ContentBlockView']) => {
      const block = narrowBlock(raw)
      if (block === null) return
      queryClient.setQueryData<EditorLesson>(key, (lesson) =>
        lesson === undefined
          ? lesson
          : { ...lesson, blocks: lesson.blocks.map((item) => (item.id === block.id ? block : item)) },
      )
    },
    refetch: () => queryClient.invalidateQueries({ queryKey: key }),
  }
}

export type LessonFields = {
  sessionType: SessionType
  hours?: number
  estimatedMinutes?: number
  code?: string
}

export function useSaveLessonFields(lessonId: string) {
  const cache = useLessonCache(lessonId)
  return useMutation({
    mutationFn: (fields: LessonFields) =>
      unwrap(
        api.PATCH('/api/v1/teacher/lessons/{id}', {
          params: { path: { id: lessonId } },
          body: {
            sessionType: fields.sessionType,
            ...(fields.hours !== undefined && { allocatedHours: fields.hours }),
            ...(fields.estimatedMinutes !== undefined && { estimatedMinutes: fields.estimatedMinutes }),
            ...(fields.code !== undefined && { code: fields.code }),
          },
        }),
      ),
    onSuccess: cache.setLesson,
  })
}

/** Links or unlinks a lesson required before this one, or a learning outcome the lesson serves. */
export function useLessonLink(lessonId: string, kind: 'prerequisites' | 'outcomes') {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ targetId, linked }: { targetId: string; linked: boolean }) => {
      if (kind === 'prerequisites') {
        const options = { params: { path: { id: lessonId, requiredLessonId: targetId } } }
        await unwrap(
          linked
            ? api.POST('/api/v1/teacher/lessons/{id}/prerequisites/{requiredLessonId}', options)
            : api.DELETE('/api/v1/teacher/lessons/{id}/prerequisites/{requiredLessonId}', options),
        )
      } else {
        const options = { params: { path: { id: lessonId, outcomeId: targetId } } }
        await unwrap(
          linked
            ? api.POST('/api/v1/teacher/lessons/{id}/outcomes/{outcomeId}', options)
            : api.DELETE('/api/v1/teacher/lessons/{id}/outcomes/{outcomeId}', options),
        )
      }
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: authoringKeys.lesson(lessonId) }),
  })
}

export function useSaveLessonText(lessonId: string) {
  const cache = useLessonCache(lessonId)
  return useMutation({
    mutationFn: ({ locale, title, summary }: { locale: Locale; title: string; summary: string }) =>
      unwrap(
        api.PUT('/api/v1/teacher/lessons/{id}/translations/{locale}', {
          params: { path: { id: lessonId, locale } },
          body: { title, ...(summary !== '' && { summary }) },
        }),
      ),
    onSuccess: cache.setLesson,
  })
}

export function useLessonStatusInEditor(lessonId: string) {
  const cache = useLessonCache(lessonId)
  return useMutation({
    mutationFn: (status: PublicationStatus) =>
      unwrap(
        api.PUT('/api/v1/teacher/lessons/{id}/status', {
          params: { path: { id: lessonId } },
          body: { status },
        }),
      ),
    onSuccess: (raw) => {
      cache.setLesson(raw)
      void cache.queryClient.invalidateQueries({ queryKey: ['authoring', 'tree'] })
    },
  })
}

export type NewBlock = {
  type: BlockType
  settings: JsonObject
  fileId?: string
  visibility: BlockVisibility
  /** The first language's content, written right after the block is made. */
  payload?: { locale: Locale; value: JsonObject }
}

export function useCreateBlock(lessonId: string) {
  const cache = useLessonCache(lessonId)
  return useMutation({
    mutationFn: async (block: NewBlock) => {
      const created = await unwrap(
        api.POST('/api/v1/teacher/lessons/{lessonId}/blocks', {
          params: { path: { lessonId } },
          body: {
            blockType: block.type,
            settings: block.settings,
            visibility: block.visibility,
            ...(block.fileId !== undefined && { fileId: block.fileId }),
          },
        }),
      )
      if (created.id !== undefined && block.payload !== undefined) {
        await unwrap(
          api.PUT('/api/v1/teacher/blocks/{id}/translations/{locale}', {
            params: { path: { id: created.id, locale: block.payload.locale } },
            body: { payload: block.payload.value },
          }),
        )
      }
      return created.id
    },
    onSettled: cache.refetch,
  })
}

export function useSaveBlockPayload(lessonId: string) {
  const cache = useLessonCache(lessonId)
  return useMutation({
    mutationFn: ({ id, locale, payload }: { id: string; locale: Locale; payload: JsonObject }) =>
      unwrap(
        api.PUT('/api/v1/teacher/blocks/{id}/translations/{locale}', {
          params: { path: { id, locale } },
          body: { payload },
        }),
      ),
    onSuccess: cache.setBlock,
  })
}

export type BlockFields = {
  settings?: JsonObject
  fileId?: string
  visibility?: BlockVisibility
  required?: boolean
}

export function useSaveBlockFields(lessonId: string) {
  const cache = useLessonCache(lessonId)
  return useMutation({
    mutationFn: ({ id, fields }: { id: string; fields: BlockFields }) =>
      unwrap(
        api.PATCH('/api/v1/teacher/blocks/{id}', {
          params: { path: { id } },
          body: {
            ...(fields.settings !== undefined && { settings: fields.settings }),
            ...(fields.fileId !== undefined && { fileId: fields.fileId }),
            ...(fields.visibility !== undefined && { visibility: fields.visibility }),
            ...(fields.required !== undefined && { isRequired: fields.required }),
          },
        }),
      ),
    onSuccess: cache.setBlock,
  })
}

export function useDeleteBlock(lessonId: string) {
  const cache = useLessonCache(lessonId)
  return useMutation({
    mutationFn: (id: string) =>
      unwrap(api.DELETE('/api/v1/teacher/blocks/{id}', { params: { path: { id } } })),
    onSettled: cache.refetch,
  })
}

export function useReorderBlocks(lessonId: string) {
  const cache = useLessonCache(lessonId)
  const key = authoringKeys.lesson(lessonId)
  return useMutation({
    mutationFn: (orderedBlockIds: string[]) =>
      unwrap(
        api.PUT('/api/v1/teacher/lessons/{lessonId}/blocks/reorder', {
          params: { path: { lessonId } },
          body: { orderedBlockIds },
        }),
      ),
    onMutate: async (ordered) => {
      await cache.queryClient.cancelQueries({ queryKey: key })
      const previous = cache.queryClient.getQueryData<EditorLesson>(key)
      if (previous !== undefined) {
        cache.queryClient.setQueryData<EditorLesson>(key, {
          ...previous,
          blocks: ordered
            .flatMap((id) => previous.blocks.filter((block) => block.id === id))
            .map((block, position) => ({ ...block, position })),
        })
      }
      return previous
    },
    onError: (_error, _ordered, previous) => {
      if (previous !== undefined) cache.queryClient.setQueryData(key, previous)
    },
    onSettled: cache.refetch,
  })
}

/** Uploads a file (an image, a diagram, a handout) for the students of the course to open. */
export function useUploadFile() {
  return useMutation({
    mutationFn: async (file: File) => {
      const form = new FormData()
      form.append('file', file)
      const detail = await unwrap(
        api.POST('/api/v1/files', {
          params: { query: { visibility: 'AUTHORIZED' } },
          body: { file: '' },
          bodySerializer: () => form,
        }),
      )
      if (detail.id === undefined) throw new Error('Upload without id')
      return { id: detail.id, name: detail.originalFilename ?? file.name }
    },
  })
}

/** The module of a lesson: where the lesson editor learns its course. */
export function useModuleQuery(moduleId: string | undefined) {
  return useQuery({
    queryKey: ['authoring', 'module', moduleId],
    queryFn: () =>
      unwrap(api.GET('/api/v1/teacher/modules/{id}', { params: { path: { id: moduleId ?? '' } } })),
    enabled: moduleId !== undefined,
    staleTime: Infinity,
  })
}
