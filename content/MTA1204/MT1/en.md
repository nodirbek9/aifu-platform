---
title: Mastering the topics of the lectures and practical sessions
summary: Independent work: a plan for mastering the course topics, the key notions and check questions for every module, the method of "running an algorithm by hand", problem sets for practice and criteria for assessing your own knowledge.
---

## Why it matters

The subject of algorithms cannot be mastered by listening to lectures alone: to say you understand an algorithm, you must **run it by hand**, **write it in code** and **explain its complexity**. This independent work gives you a plan for reviewing the course topics systematically: what you need to know for every module, how to check yourself and where to practise.

:::note
**After this independent work you will be able to:**

- review the course topics systematically, module by module;
- run every algorithm by hand on a small example and check the result;
- assess your knowledge on the scale "recognise — explain — write — analyse";
- find suitable problems for practice.
:::

## The mastery scale

Rate every topic at four levels:

| Level | Sign | Check |
|---|---|---|
| 1. Recognise | I know its name and purpose | answer "what does this algorithm do?" in one sentence |
| 2. Explain | I can describe how it works in words | explain it to a friend in 2 minutes |
| 3. Write | I can write it in code without the book | a working program in 20 minutes |
| 4. Analyse | I can prove its complexity and know its edge cases | build the worst input yourself |

When preparing for the exam, every main algorithm should reach at least level 3.

## A plan by module

| Module | Key notions | Check yourself |
|---|---|---|
| Algorithms and complexity | properties of an algorithm, the Turing machine, *O*/Ω/Θ, recurrence relations | justify the solution of *T*(*n*) = 2*T*(*n*/2) + *n* |
| Search | linear, binary, interpolation, Fibonacci search, the hash table | run binary search by hand on a 16-element array |
| Sorting | bubble, insertion, Shell, heap, quick, merge sort; stability; external sorting | write every sort step by step on `5 2 9 1 7` |
| Parallel sorting | sorting networks, odd–even sort, Batcher's network, the 0-1 principle | draw a 4-element network and pass `4 3 2 1` through it |
| Dynamic structures | stack, queue, deque, linked lists | trace a circular buffer by hand through 5 commands |
| Graphs | matrix and lists, BFS, Dijkstra, Floyd–Warshall | fill in Dijkstra's table on a 5-vertex graph |
| Trees | BST, traversals, balance, segment and Fenwick trees | insert `8 3 10 1 6 14` into a BST and write all three traversals |

## Running an algorithm by hand

The most effective way to review is to run an algorithm **on paper** on a small input:

1. Choose an input of 5–8 elements that includes an edge case (equal elements, a negative number, a single element).
2. At every step write the state of the variables in a table (indices, the array, the stack, the distances).
3. Compare the result with that of a ready program.
4. Count the comparisons (or steps) and compare them with the complexity estimate.

If at some step you say "I don't know what happens" — re-read exactly that part of the lecture text.

## The independent work assignment

1. **A mastery table** — mark your current level (1–4) for every algorithm in the module table above.
2. **Running by hand** — for every algorithm below level 3, make one table of running it by hand.
3. **Code** — write at least five algorithms (one from each module) without the book and check them against the course's sample examples.
4. **Practice** — solve at least two problems per module from the collections below and keep the links to your solutions.
5. **A conclusion** — which topic was hardest and how you mastered it (5–7 sentences).

:::warning
**Grading criteria and common mistakes**

- The tables of running by hand must be made independently, not copied from a program's output.
- Writing just "it worked" is not enough: for every algorithm show its complexity and its worst input.
- Copying another student's code — the work is not accepted.
- Do not leave reviewing until the eve of the exam: one module a week is the most effective plan.
:::

:::tip
Explain what you have learned to someone else: if you can explain an algorithm to a classmate in 5 minutes, you really know it. Whatever you could not explain is your next topic to review.
:::

## Summary

- Mastering an algorithm has four levels: recognise, explain, write, analyse.
- Every module has key notions and an exercise of running by hand.
- Running by hand is the most reliable way to find mistakes and get a feel for complexity.
- Regular practice and explaining to others consolidate knowledge.

## Check yourself

:::check
Q: What is the sign of knowing an algorithm at the "write" level?
A: Writing it without the book as a working program in a short time.
Q: How is an input for running by hand chosen?
A: Small (5–8 elements) and including an edge case: equal elements, a negative number or a single element.
Q: Why are the steps counted when running by hand?
A: To compare them with the complexity estimate and so check the theoretical analysis in practice.
:::

:::link https://cses.fi/problemset/
The CSES problem set
Graded problems on sorting, searching, graphs and trees.
:::

:::link https://visualgo.net/en
VisuAlgo
Animations of almost every algorithm and structure of the course.
:::
