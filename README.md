Jruby-leaks
===========

Pack of different scenarios (exposing leaks) in which Jruby ScriptingContainer's are used to process Ruby script.


Basically to recreate different configuration one have to run ReleasingResourcesTest main method. Every 250 ms, new Evaluator is created and simple hard coded code is run within newly created SC.

below are named cases where we can observer problems with old gen

1) MainThreadStartAndParseAndTerminatesPooledRuns
2) MainThreadStartAndParseNotPooledRunsAndTerminates

Above two are caused by using SC from different threads.

Probably caused by the fact that on termination some references from threadLocals, from different thread than the one which terminated SC, still exist

3) SequentialWithCustomOutputPooledDoingAll

Above one is exposed because custom stdout is used. It is connected with lack of unregistering descriptors from static map filenoMapDescriptors in ChannelDescriptor.
