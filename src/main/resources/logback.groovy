import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.PatternLayout
import static ch.qos.logback.classic.Level.INFO
scan("30 seconds")
//def LOG_PATH = "logs"
//def LOG_ARCHIVE = "${LOG_PATH}/archive"
appender("Console-Appender", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%date{ISO8601} - %logger{15}- %level: %msg%n"
  }
}
//appender("File-Appender", FileAppender) {
//  file = "${LOG_PATH}/logfile.log"
//  encoder(PatternLayoutEncoder) {
////    pattern = "%msg%n"
//    outputPatternAsHeader = false
//  }
//}

logger("org.apache.tomcat.jdbc.pool", ERROR, ["Console-Appender"], false)
//logger("org.apache.tomcat.jdbc.pool", ERROR, ["Console-Appender", "File-Appender"], false)
root(WARN, ["Console-Appender"])