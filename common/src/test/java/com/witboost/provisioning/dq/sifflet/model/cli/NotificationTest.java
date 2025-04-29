package com.witboost.provisioning.dq.sifflet.model.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NotificationTest {

    @Test
    void testEmailNotificationCreation() {
        Notification.Email email = new Notification.Email("EmailName");

        assertNotNull(email);
        assertEquals("Email", email.getKind());
        assertEquals("EmailName", email.getName());
    }

    @Test
    void testEmailNotificationEquality() {
        Notification.Email email1 = new Notification.Email("Email1");
        Notification.Email email2 = new Notification.Email("Email1");
        Notification.Email email3 = new Notification.Email("Email2");

        assertEquals(email1, email2);
        assertNotEquals(email1, email3);
    }

    @Test
    void testEmailNotificationToString() {
        Notification.Email email = new Notification.Email("TestEmail");
        assertTrue(email.toString().contains("TestEmail"));
    }

    @Test
    void testCanEqual() {
        Notification notification1 = new Notification.Email("EmailNotification");
        Notification notification2 = new Notification.Email("AnotherEmailNotification");

        assertTrue(notification1.canEqual(notification2));
        assertFalse(notification1.canEqual(null));
    }

    @Test
    void testEmptyConstructor() {
        Notification notification = new Notification();
        assertNull(notification.getKind());
    }

    @Test
    void testHashCode() {
        Notification notification1 = new Notification.Email("EmailNotification");
        Notification notification2 = new Notification.Email("EmailNotification");

        assertEquals(notification1.hashCode(), notification2.hashCode());
        Notification notificationWithNull = new Notification.Email(null);
        assertNotNull(notificationWithNull.hashCode());
    }

    @Test
    void testEquals() {
        Notification notification1 = new Notification.Email("EmailNotification");
        Notification notification2 = new Notification.Email("EmailNotification");
        Notification notification3 = new Notification.Email("AnotherEmailNotification");

        assertEquals(notification1, notification1);
        assertEquals(notification1, notification2);
        assertNotEquals(notification1, notification3);
        assertNotEquals(notification1, null);
    }
}
