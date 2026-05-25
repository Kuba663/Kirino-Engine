# ForkJoinPoolUtils

> Relevant classes:
> <br>· `com.cleanroommc.kirino.utils.ForkJoinPoolUtils`
> <br>· `com.cleanroommc.kirino.engine.ShutdownManager`

`ForkJoinPoolUtils.newWorkStealingPool(...)` helps you to create work stealing pools and 
`ForkJoinPoolUtils.shutdownPool(...)` helps you to close the pools.

The default `ForkJoinPoolUtils.newWorkStealingPool(...)` method overload provides/allows:
- Custom pool name
- Automatically calculated parallelism: `parallelism = cores - 1`
- Async mode = true (FIFO scheduling)
- Has a default error logger that catches unhandled exceptions

Common usage pattern:
```java
ForkJoinPool myPool = ForkJoinPoolUtils.newWorkStealingPool("MyPoolName");

ShutdownManager.registerAsync(() -> {
    ForkJoinPoolUtils.shutdownPool(myPool, 5); // timeout = 5s
});
```
You can expect:
```
MyPoolName-worker-1
MyPoolName-worker-2
...
```
