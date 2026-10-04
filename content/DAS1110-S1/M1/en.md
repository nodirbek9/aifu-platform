---
title: Basic concepts of programming
summary: Algorithm and program, how a computer runs a program, compiler and interpreter, a first program: input, computation, output.
---

## Why it matters

At the end of every semester the dean's office works out the final score of 600 students: the continuous, midterm and final assessment points are added up, the result is compared with the grading scale and a list of students with academic debt is drawn up. By hand this takes several days, and every calculation can go wrong. A computer does the same job in a second and without mistakes — but only if we **tell it exactly what to do**.

Explaining a job to a computer is programming. In this lesson we look at the basic ideas of "talking" to a computer: what an algorithm is, how a program is built, how the computer runs it and what the simplest program looks like.

:::note
**After this lesson you will be able to:**

- explain the difference between an algorithm and a program;
- list the main properties of an algorithm and illustrate them with an example;
- compare how a compiler and an interpreter work;
- read and write, in Python, Java and C++, a first program that reads data, computes and prints the result.
:::

## Algorithm

An **algorithm** is a finite sequence of steps, carried out in a strictly defined order, that solves a problem.

Algorithms are everywhere in everyday life. A recipe for making tea, the steps for withdrawing cash from an ATM, the stages of applying to a university — all of these are algorithms. For example, the algorithm for finding the larger of two numbers:

1. Take the first number and call it *a*.
2. Take the second number and call it *b*.
3. If *a* > *b*, the answer is *a*; otherwise the answer is *b*.
4. State the answer.

An algorithm stays the same whatever it is written in: words, a flowchart or a programming language. The most important work in programming is finding the algorithm; translating it into a language is the next step.

### Properties of an algorithm

| Property | Meaning | What happens if it is broken |
|---|---|---|
| **Discreteness** | The algorithm consists of separate, successive steps | It is unclear what to do and when |
| **Definiteness** | Every step is understood in exactly one way | "Add salt to taste" — everyone adds a different amount |
| **Finiteness** | The algorithm ends after a finite number of steps | The program "hangs" and gives no result |
| **Effectiveness** | The algorithm produces a definite result | Work is done, but there is no answer |
| **Generality** | The algorithm works for every problem of its kind | An "addition" algorithm that only works for 5 and 7 is useless |

### Ways to write an algorithm

- **In words** — numbered steps, as above. Convenient for simple problems.
- **Flowchart** — geometric shapes: an oval for start and end, a rectangle for an action, a diamond for a condition, a parallelogram for input and output. Good for seeing conditions and repetitions.
- **Pseudocode** — a notation that looks like a programming language but has no strict rules.
- **In a programming language** — the only form a computer can carry out.

## Program and programming language

A **program** is an algorithm written in a language a computer understands. A **programming language** is an artificial language with strict rules for writing programs.

Every programming language has two sides:

- **Syntax** — the rules of writing: which words are used, where brackets, semicolons and indentation go. If the syntax is broken, the program does not start at all.
- **Semantics** — the meaning of what is written: what the program actually does. A program with correct syntax but wrong meaning runs, but gives the wrong answer. Such errors are much harder to find.

In this course we look at three languages side by side: **Python**, **Java** and **C++**. All three are widely used in industry, but each works in its own way — we will see how below.

## How a computer runs a program

A processor understands only **machine code** — very simple instructions in binary form (add two numbers, write to memory, go to the next instruction). The text we write is called **source code**. There are two main ways to turn it into machine code.

A **compiler** translates the whole program in advance, once, and produces an executable file. That file then runs fast, without any translation. If there is an error, the compiler reports it during translation and no file is produced. C++ works this way.

An **interpreter** reads the program line by line while it runs and executes each line immediately. A program is easy to try out quickly, but it runs more slowly, and an error appears only when its line's turn comes. Python works this way.

**Java** takes a middle road: the compiler turns the program into **bytecode**, which the **Java Virtual Machine (JVM)** executes. That is why one Java program runs on Windows and on Linux without being compiled again.

| | C++ | Java | Python |
|---|---|---|---|
| Translation | Compiler → machine code | Compiler → bytecode → JVM | Interpreter |
| When errors are found | At compile time | At compile time | While running |
| Speed | Very high | High | Moderate |
| Size of a first program | A few lines | Needs a class and a `main` method | One line |

## The structure of a program: input, processing, output

Most practice problems consist of three parts:

1. **Input** — the program receives data: from the keyboard, a file or the network.
2. **Processing** — operations are performed on the data.
3. **Output** — the result goes to the screen, a file or another program.

While the program runs, data is kept in **variables**. A variable is a named place in memory: `a = 5` means "write 5 into the place called *a*".

The following program reads two integers and prints their sum. Compare the versions in the three languages.

```python
# Read two numbers (each on its own line)
a = int(input())
b = int(input())

# Compute the sum and print it
s = a + b
print("Sum:", s)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        // Read two numbers
        int a = in.nextInt();
        int b = in.nextInt();

        // Compute the sum and print it
        int s = a + b;
        System.out.println("Sum: " + s);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    // Read two numbers
    int a, b;
    cin >> a >> b;

    // Compute the sum and print it
    int s = a + b;
    cout << "Sum: " << s << endl;
    return 0;
}
```

## The program step by step

Let the input be `12` and `30`. The program works like this:

1. The first line is read and turned into an integer: *a* = 12. In Python `input()` returns text, so `int(...)` is needed; in Java `nextInt()` and in C++ `cin >>` read the number directly.
2. The second number is read: *b* = 30.
3. `s = a + b` — the sum is computed and stored in the variable *s*: *s* = 42.
4. The result is printed: `Sum: 42`.

All three programs carry **the same algorithm**; only the syntax differs. In Java and C++ the type of a variable (`int` — an integer) is written in advance; in Python the type follows from the value. The Java program lives inside `class Main`, the C++ program inside the `main()` function — that is where execution starts.

:::warning
**Common mistakes**

- **Forgetting `int()` in Python.** `input()` returns text: `"12" + "30"` gives `"1230"`, not 42.
- **Leaving out the semicolon (`;`) in C++ and Java.** That is a syntax error — the program will not compile.
- **Mixing up capital and small letters.** `Print`, `Main`, `System.Out` are different names. All three languages are case-sensitive.
- **Writing code before thinking about the problem.** First work out the algorithm on paper: what goes in, what comes out, what happens in between.
:::

:::tip
Before writing any program, ask yourself three questions: **what is given? what must be found? how?** The answer to these three questions is a ready algorithm.
:::

## Summary

- An **algorithm** is a precise, finite sequence of steps that solves a problem; a **program** is that algorithm written in a programming language.
- The main properties of an algorithm: discreteness, definiteness, finiteness, effectiveness, generality.
- A **compiler** translates the whole program in advance (C++), an **interpreter** works line by line while running (Python), and Java works through bytecode and the JVM.
- **Syntax** is the rules of writing, **semantics** is the meaning; both have to be right.
- A simple program has three parts: input, processing, output; data is kept in variables.

## Check yourself

:::check
Q: What is the difference between an algorithm and a program?
A: An algorithm is the steps that solve a problem; it can be written in any form (words, a flowchart). A program is that very algorithm written in a programming language a computer can carry out.
Q: Which property of an algorithm can the step "wait until the water boils" break?
A: Finiteness: if the stove is not switched on, the water never boils and the algorithm never ends.
Q: What is the main difference between a compiler and an interpreter?
A: A compiler translates the whole program in advance and produces an executable file; an interpreter translates and executes the program line by line while it runs.
Q: What does the Python program `print(input() + input())` print for the input `12` and `30`, and why?
A: It prints `1230`, because `input()` returns text, and adding two pieces of text joins them. To add them as numbers you need `int(input())`.
Q: How does a syntax error differ from a logic (semantic) error?
A: With a syntax error the program does not start at all (the compiler or interpreter reports it). With a logic error the program runs but gives a wrong result.
:::

:::link https://docs.python.org/3/tutorial/introduction.html
The official Python tutorial: an informal introduction
First steps in Python, working with numbers and text.
:::

:::link https://en.cppreference.com/w/cpp/language/main_function
cppreference: the main function
Reference on where a C++ program starts.
:::
