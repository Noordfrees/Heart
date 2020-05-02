# Heart
Yet another Hearts-like game. Written in Java.

## Getting Started

No installation needed. Just clone or download the repository and run `javac Game.java` in the base directory. You need to have a Java Development Kit, version 8 or later, installed in order to use `javac`. I recommend OpenJDK.

After compiling, you can start Heart from the base directory using `java Game`.

## The Game

Heart is a card game for four players with the aim of gaining as *few* points as possible. The game is played over several rounds until one or more players reach 100 points.

At the beginning of each round, 13 cards are dealt to each player. Every player now selects three cards to pass. In the first round, the cards are then passed to the player on the left; in the second round to the player on the right; in the third round to the player sitting opposite on the table; in the fourth round, no cards are passed; and so on.

A round consists of 13 ‘tricks’. In every trick, the four players play one of their cards round-robin. The first trick of a round is started by the player who holds the 2 of clubs, and this is the card he has to play. The other players then play a card of the same card colour (clubs, spades, diamonds, or hearts) if they have one, otherwise any card. When all four players have played a card, the player who played the highest card of the leading colour ‘takes’ the trick, that is, he gets points for those cards: Every Heart card is worth 1 point, and the Queen of Spades is worth 13 points. (Remember, the aim is to *avoid* getting points!) The player who took the trick also starts the next trick with any card of his choosing.

There are some exceptions to these rules:
- It is not allowed to play point cards on the first trick, unless you have *only* point cards, in which case you may play Hearts (but not the Queen of Spades).
- You may not use a Heart card to start a trick, unless hearts have been played already in this round or you have nothing but Hearts left.
- If a player manages to take all fourteen point cards in a round, he gets not +26 but -26 points (‘Shooting the Moon’). If he manages to take *all* tricks of the round, he gets another -26 points, totalling -52 points (‘Shooting the Sun’). Be aware that attempting to shoot the moon or sun is a rewarding but risky strategy – if even one point card escapes you, you will get many malus points instead of the hoped-for bonus…

---

You can see the score of each player as well as the number of points they are gaining in the current round next to their names. You control the southern player; the others are controlled by computer players.

## Website

[Repository](https://github.com/Noordfrees/Heart)

[Bug reports go here](https://github.com/Noordfrees/Heart/issues)

## Credits

Created by [Benedikt Straub (@Noordfrees)](https://github.com/Noordfrees) in his spare time. I hope you like my work.

The card images are edited versions of a card set distributed with [Aisleriot](https://wiki.gnome.org/Apps/Aisleriot).

## License

GPL3. See the file [LICENSE](https://github.com/Noordfrees/Heart/blob/master/LICENSE) for details.
