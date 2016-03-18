/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.notification;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author andrew00x
 */
public class EventServiceTest {
    private EventService bus;

    @BeforeMethod
    public void setUp() {
        bus = new EventService();
    }

    @Test
    public void testSimpleEvent() {
        final List<Object> events = new ArrayList<>();
        bus.subscribe(new EventSubscriber<Date>() {
            @Override
            public void onEvent(Date event) {
                events.add(event);
            }
        });
        bus.subscribe(new EventSubscriber<String>() {
            @Override
            public void onEvent(String event) {
                events.add(event);
            }
        });
        bus.subscribe(new EventSubscriber<Long>() {
            @Override
            public void onEvent(Long event) {
                events.add(event);
            }
        });
        Date date = new Date();
        bus.publish(date);
        bus.publish("hello");
        bus.publish(123L);
        Assert.assertEquals(events.size(), 3);
        Assert.assertTrue(events.contains(date));
        Assert.assertTrue(events.contains("hello"));
        Assert.assertTrue(events.contains(123L));
        // ignored
        bus.publish(new Object());
        Assert.assertEquals(events.size(), 3);
    }

    interface I {
    }

    static class Listener implements I, EventSubscriber<String> {
        final List<String> events = new ArrayList<>();

        @Override
        public void onEvent(String event) {
            events.add(event);
        }
    }

    static class ExtListener extends Listener {
    }

    @Test
    public void testRegisterHierarchicalListener() {
        ExtListener listener = new ExtListener();
        bus.subscribe(listener);
        bus.publish("hello");
        Assert.assertEquals(listener.events.size(), 1);
        Assert.assertEquals(listener.events.get(0), "hello");
    }

    static class Event {
        String data;

        Event() {
            this("event");
        }

        Event(String data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return data;
        }
    }

    static class ExtEvent extends Event {
        ExtEvent() {
            super("ext_event");
        }
    }

    @Test
    public void testHierarchicalEvent() {
        final List<String> events = new ArrayList<>();
        // register two listeners.
        // 1. Accept Event type.
        bus.subscribe(new EventSubscriber<Event>() {
            @Override
            public void onEvent(Event event) {
                events.add(String.format("1:%s", event));
            }
        });

        // 2. Accept ExtEvent type.
        bus.subscribe(new EventSubscriber<ExtEvent>() {
            @Override
            public void onEvent(ExtEvent event) {
                events.add(String.format("2:%s", event));
            }
        });

        bus.publish(new Event());
        Assert.assertEquals(events.size(), 1);
        Assert.assertEquals(events.get(0), "1:event");
        events.clear();
        bus.publish(new ExtEvent());
        Assert.assertEquals(events.size(), 2);
        Assert.assertTrue(events.contains("1:ext_event"));
        Assert.assertTrue(events.contains("2:ext_event"));
    }

    @Test
    public void testSubscribingOnChildEvents() {
        final List<String> events = new ArrayList<>();
        // Accept ExtEvent type.
        bus.subscribe(new EventSubscriber<Event>() {
            @Override
            public void onEvent(Event event) {
                events.add(String.format("1:%s", event));
            }
        }, ExtEvent.class);

        bus.publish(new Event());
        bus.publish(new ExtEvent());
        Assert.assertEquals(events.size(), 1);
        Assert.assertTrue(events.contains("1:ext_event"));
    }

    @Test
    public void testUnsubscribe() {
        final List<String> events = new ArrayList<>();
        EventSubscriber<Event> l = new EventSubscriber<Event>() {
            @Override
            public void onEvent(Event event) {
                events.add(event.data);
            }
        };
        bus.subscribe(l);
        bus.publish(new Event());
        Assert.assertEquals(events.size(), 1);
        bus.unsubscribe(l);
        events.clear();
        bus.publish(new Event());
        Assert.assertEquals(events.size(), 0);
    }
}
