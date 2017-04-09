package net.basiccloud.light.client.internal;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class PriorityTest {
    @Test
    public void defaultPriorities() throws Exception {
        int[] priorities = Priority.defaultPriorities(3);
        assertThat(priorities.length).isEqualTo(3);
        assertThat(priorities[0]).isEqualTo(1);
        assertThat(priorities[1]).isEqualTo(1);
        assertThat(priorities[2]).isEqualTo(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultPriorities_zero() throws Exception {
        Priority.defaultPriorities(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultPriorities_lessZero() throws Exception {
        Priority.defaultPriorities(-1);
    }
}