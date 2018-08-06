package com.bitquest.bitquest;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import com.bitquest.bitquest.BitQuest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.text.ParseException;

public class BitQuestTests {

    @Test
    public void testWalletFunctionality() {
        BitQuest bitquest = new BitQuest();
        PlayerJoinEvent mockEvent = PowerMockito.mock(PlayerJoinEvent.class);
        Player mockPlayer = mock(Player.class);
        try {
            User user = new User(bitquest, mockPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}