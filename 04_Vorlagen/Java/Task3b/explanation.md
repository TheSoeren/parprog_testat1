# Task 3
## 3a
As seen in the compatibility matrix there is an important difference between a read lock and an upgradable read lock. The upgradable read lock can potentially be a write lock, which results in it being unable to run parallel with another upgradable read lock. A plain read lock however can coexist with a upgradable read lock.