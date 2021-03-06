package org.obiba.opal.search;

import org.obiba.magma.ValueTable;
import org.obiba.opal.core.support.MessageLogger;

import java.util.*;

/**
 *
 */
public class MessageCollector {

    private static final String WARN_PREFIX = "WARN: ";
    private static final String ERROR_PREFIX = "ERROR: ";

    private Map<String, List<Message>> map = new HashMap<>();

    public List<Message> getMessages(String tableRef) {

        List<Message> list = null;
        synchronized(map) {
            list = map.get(tableRef);
        }
        return list == null ? Collections.<Message>emptyList() : list;
    }

    public List<Message> getMessages(ValueTable table) {
        return getMessages(table.getTableReference());
    }

    private void setMessages(String ref, List<Message> messages) {
        synchronized (map) {
            map.put(ref, messages);
        }
    }

    public Task createTask(ValueTable table) {
        return createTask(table.getTableReference());
    }

    public Task createTask(String tableRef) {
        return new Task(tableRef);
    }

    public class Task implements MessageLogger {

        private final String ref;
        private List<Message> list = new ArrayList<>();

        private Task(String ref) {
            this.ref = ref;
        }

        @Override
        public void debug(String msgFormat, Object... args) {
            add(msgFormat, args);
        }

        @Override
        public void info(String msgFormat, Object... args) {
            add(msgFormat, args);
        }

        @Override
        public void warn(String msgFormat, Object... args) {
            add(WARN_PREFIX + msgFormat, args);
        }

        @Override
        public void error(String msgFormat, Object... args) {
            add(ERROR_PREFIX + msgFormat, args);
        }

        private void add(String msgFormat, Object... args) {
            list.add(new Message(System.currentTimeMillis(), String.format(msgFormat, args)));
        }


        public List<Message> getErrorMessages() {
            List<Message> result = new ArrayList<>();
            for (Message msg: list) {
                if (msg.getMessage().startsWith(ERROR_PREFIX)) {
                    result.add(msg);
                }
            }
            return result;
        }
        /**
         * Closes this task, making sure the collected messages are kept.
         */
        public void close() {
            setMessages(ref, list); //replaces the messages
        }

    }

}
