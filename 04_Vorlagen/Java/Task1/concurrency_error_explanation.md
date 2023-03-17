# Task1: Broken Cyclic Barrier (Theory)
## Explanation of the concurrency error
For this class in the method `multiRounds` a **Deadlock** is created. This deadlock can occur, because
we are overriding the variable, which stores the latch, while it is possible the previous latch is still
being awaited. Since the previous latch only exists in memory and is no longer accessible through the
variable `latch` the code `latch.countDown();` will never be able to fully drain this previous latch and will
only affect the latch, which is currently stored in the variable `latch`.

As soon as a thread counts down in a latch, before the previous rounds `thread 0` could create a new latch, the previous round is now locked out of ever fully running through. 