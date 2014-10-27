Jruby-leaks
===========

Pack of different scenarios (exposing leaks) in which Jruby ScriptingContainer's are used to process Ruby script.


Basically to recreate different configuration one have to run ReleasingResourcesTest main method. Every 250 ms, new Evaluator is created and simple hard coded ruby code is run within newly created SC. To change scenario one have to change string passed to getEvaluator method.

below are named cases where we can observe problems with old gen

1) "MainThreadStartAndParseAndTerminatesPooledRuns"
2) "MainThreadStartAndParseNotPooledRunsAndTerminates"

Above two are caused by using SC from different threads.

Probably caused by the fact that on termination some references from threadLocals, from different thread than the one which terminated SC, still exist

3) "SequentialWithCustomOutputPooledDoingAll"

Above one is exposed because custom stdout is used. It is connected with lack of unregistering descriptors from static map filenoMapDescriptors in ChannelDescriptor.


In etc file one have to also define path to RubyDir where sources of ruby are kept. It can be colon separated list.
