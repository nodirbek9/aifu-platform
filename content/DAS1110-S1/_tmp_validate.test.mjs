import test from 'node:test';
import assert from 'node:assert';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { validateAssess } from '../../tools/content/assess-schema.mjs';

const here = path.dirname(fileURLToPath(import.meta.url));

for (const lesson of ['A7', 'A8', 'A9']) {
  test(`assess.json ${lesson} validates`, () => {
    const file = path.join(here, lesson, 'assess.json');
    const data = JSON.parse(fs.readFileSync(file, 'utf8'));
    const problems = validateAssess(data);
    if (problems.length) console.log(lesson + ':\n' + problems.join('\n'));
    assert.deepEqual(problems, []);
  });
}
