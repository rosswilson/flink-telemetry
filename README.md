# Flink Telemetry

An investigation into using [Apache Flink](https://ci.apache.org/projects/flink/flink-docs-release-1.2/) for telemetry monitoring.

For a while now I've been leveling up on my DevOps skills, and have developed a keen interest in service monitoring and alerting. Telemetry (to me) is the collection of metrics and other data, the processing of that data info valuable information, and the triggering of actionable alerts.

## Getting Started

To run and test your application use SBT invoke: 'sbt run'

In order to run your application from within IntelliJ, you have to select the classpath of the 'mainRunner' module in the run/debug configurations.
Simply open 'Run -> Edit configurations...' and then select 'mainRunner' from the "Use classpath of module" dropbox. 
