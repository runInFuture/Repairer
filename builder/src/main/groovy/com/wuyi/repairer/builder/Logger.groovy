package com.wuyi.repairer.builder

/**
 * A Simple logger used in gradle env.
 */
class Logger {
    static Logger instance

    static Logger getInstance() {
        if (instance == null) {
            instance = new Logger()
        }
        return instance
    }

    void log(String msg) {
        println msg
    }
}