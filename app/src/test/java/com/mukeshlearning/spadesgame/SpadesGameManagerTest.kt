package com.mukeshlearning.spadesgame
import com.mukeshlearning.spadesgame.domain.SpadesGameLogic
import junit.framework.TestCase.assertEquals
import org.junit.Test

class SpadesGameManagerTest {
    @Test
    fun `each player should get 13 cards`() {
        val players = SpadesGameLogic.createInitialPlayers()

        val dealtPlayers = SpadesGameLogic.dealCards(players)

        dealtPlayers.forEach {
            assertEquals(13, it.hand.size)
        }
    }
}