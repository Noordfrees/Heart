import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Game {
	
	private final JFrame frame;
	private final JLabel display;
	
	public static class Card {
		public static enum Color {
			Clubs,
			Diamonds,
			Spades,
			Hearts;
		}
		public static enum Value {
			Two,
			Three,
			Four,
			Five,
			Six,
			Seven,
			Eight,
			Nine,
			Ten,
			Jack,
			Queen,
			King,
			Ace;
		}
		public final Color color;
		public final Value value;
		public Card(Color c, Value v) {
			color = c;
			value = v;
		}
	}
	
	public class Player {
		public final String name;
		public final ArrayList<Card> hand;
		public final ArrayList<Card> taken;
		private long points;
		public Player(String n) {
			name = n;
			points = 0;
			hand = new ArrayList<>();
			taken = new ArrayList<>();
		}
		public long pendingPoints() {
			if (taken.isEmpty()) {
				return 0;
			} else if (taken.size() == Card.Color.values().length * Card.Value.values().length) {
				return -52;
			}
			long p = 0;
			for (Card c : taken) {
				if (c.color == Card.Color.Hearts) {
					++p;
				} else if (c.color == Card.Color.Spades && c.value == Card.Value.Queen) {
					p += 13;
				}
			}
			return p == 26 ? -26 : p;
		}
		public long getPoints() {
			return points;
		}
		public void fetchPoints() {
			points += pendingPoints();
			taken.clear();
		}
		public void addCard(Card card) {
			final int len = hand.size();
			for (int i = 0; i < len; ++i) {
				Card c = hand.get(i);
				if (card.color.ordinal() < c.color.ordinal() || (card.color.ordinal() == c.color.ordinal() && card.value.ordinal() < c.value.ordinal())) {
					hand.add(i, card);
					return;
				}
			}
			hand.add(card);
		}
		public void reset() {
			points = 0;
			hand.clear();
			taken.clear();
		}
	}
	
	private final Player[] players;
	private int roundStarter;
	private final Card[] playing;
	private boolean endOfTrick;
	
	private ArrayList<Rectangle> cardRects;
	
	public synchronized void draw() {
		
		int w = display.getWidth();
		int h = display.getHeight();
		int whm = Math.max(w, h);
		
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		
		for (int i = 0; i < whm * 2; i++) {
			int c = 255 * i / (whm * 2);
			g.setColor(new Color(c, c, c));
			if (w > h)
				g.drawLine(i, 0, 0, i * h / w);
			else
				g.drawLine(i * w / h, 0, 0, i);
		}
		
		final int cardH = h / 8;
		final int cardW = cardH * 164 / 256;
		
		cardRects.clear();
		int cardOff = (w - cardW * players[0].hand.size()) / 2;
		int idx = 0;
		for (Card card : players[0].hand) {
			Rectangle r = new Rectangle(cardOff + cardW * (idx++), h - 2 * cardH, cardW, cardH);
			cardRects.add(r);
			g.drawImage(Toolkit.getDefaultToolkit().getImage(
					"images/" + card.color.name().toLowerCase() + "_" + card.value.name().toLowerCase() + ".png"),
				r.x, r.y, r.width, r.height, null);
		}
		cardOff = (w + cardW * players[2].hand.size()) / 2;
		idx = 0;
		for (Card card : players[2].hand) {
			g.drawImage(Toolkit.getDefaultToolkit().getImage("images/bg.png"), cardOff - cardW * (++idx), cardH, cardW, cardH, null);
		}
		cardOff = (2 * h - cardH * players[1].hand.size()) / 4;
		idx = 0;
		for (Card card : players[1].hand) {
			g.drawImage(Toolkit.getDefaultToolkit().getImage("images/bg.png"), cardW, cardOff + cardH * (idx++) / 2, cardW, cardH, null);
		}
		cardOff = (2 * h + cardH * players[3].hand.size()) / 4;
		idx = 0;
		for (Card card : players[3].hand) {
			g.drawImage(Toolkit.getDefaultToolkit().getImage("images/bg.png"), w - 2 * cardW, cardOff - cardH * (++idx) / 2, cardW, cardH, null);
		}
		
		int activePlayer = -1;
		if (playing[roundStarter] == null) {
			activePlayer = roundStarter;
		} else {
			for (int i = 0; i < playing.length; ++i) {
				if (playing[(i + 1) % playing.length] == null && playing[i] != null) {
					activePlayer = (i + 1) % playing.length;
					break;
				}
			}
		}
		for (int i = 0; i < players.length; ++i) {
			g.setFont(new Font(Font.SERIF, activePlayer == i ? Font.BOLD : Font.PLAIN, cardH / 6));
			String str = players[i].name + " (" + players[i].getPoints() + " + " + players[i].pendingPoints() + ")";
			Rectangle b = g.getFont().getStringBounds(str, g.getFontRenderContext()).getBounds();
			Point p;
			switch (i) {
				case 0: p = new Point((w - b.width) / 2, h - cardH / 3); break;
				case 2: p = new Point((w - b.width) / 2, cardH * 2 / 3); break;
				case 1: p = new Point(cardW / 2, cardH * 2 / 3); break;
				case 3: p = new Point(w - cardW / 2 - b.width, cardH * 2 / 3); break;
				default: p = null; break;
			}
			g.setColor(new Color(0x222222));
			g.drawString(str, p.x + 1, p.y + 1);
			g.setColor(new Color(0xcccccc));
			g.drawString(str, p.x, p.y);
		}
		
		if (playing[0] != null) {
			g.drawImage(Toolkit.getDefaultToolkit().getImage(
					"images/" + playing[0].color.name().toLowerCase() + "_" + playing[0].value.name().toLowerCase() + ".png"),
				(w - cardW) / 2, (h + cardH) / 2, cardW, cardH, null);
		}
		if (playing[2] != null) {
			g.drawImage(Toolkit.getDefaultToolkit().getImage(
					"images/" + playing[2].color.name().toLowerCase() + "_" + playing[2].value.name().toLowerCase() + ".png"),
				(w - cardW) / 2, (h - 3 * cardH) / 2, cardW, cardH, null);
		}
		if (playing[1] != null) {
			g.drawImage(Toolkit.getDefaultToolkit().getImage(
					"images/" + playing[1].color.name().toLowerCase() + "_" + playing[1].value.name().toLowerCase() + ".png"),
				(w - 3 * cardW) / 2, (h - cardH) / 2, cardW, cardH, null);
		}
		if (playing[3] != null) {
			g.drawImage(Toolkit.getDefaultToolkit().getImage(
					"images/" + playing[3].color.name().toLowerCase() + "_" + playing[3].value.name().toLowerCase() + ".png"),
				(w + cardW) / 2, (h - cardH) / 2, cardW, cardH, null);
		}
		
		
		
		display.setIcon(new ImageIcon(img));
		
	}
	
	private void dealCards() {
		ArrayList<Card> cards = new ArrayList<>();
		for (Card.Color c : Card.Color.values()) {
			for (Card.Value v : Card.Value.values()) {
				cards.add((int)(Math.random() * (cards.size() + 1)), new Card(c, v));
			}
		}
		roundStarter = -1;
		while (!cards.isEmpty()) {
			int i = 0;
			for (Player p : players) {
				Card c = cards.remove(0);
				p.addCard(c);
				if (c.value == Card.Value.Two && c.color == Card.Color.Clubs) {
					roundStarter = i;
				}
				++i;
			}
		}
		if (roundStarter != 0) {
			aiMoves(roundStarter);
		}
	}
	
	private void endOfTrick() {
		endOfTrick = false;
		for (int i = 0; i < playing.length; ++i) {
			if (playing[i].color == playing[roundStarter].color && playing[i].value.ordinal() > playing[roundStarter].value.ordinal()) {
				roundStarter = i;
			}
		}
		for (int i = 0; i < playing.length; ++i) {
			players[roundStarter].taken.add(playing[i]);
			playing[i] = null;
		}
		draw();
		if (players[roundStarter].hand.isEmpty()) {
			boolean gameOver = false;
			String scores = "";
			for (Player p : players) {
				p.fetchPoints();
				gameOver |= p.getPoints() >= 100;
				scores += p.name + ": " + p.getPoints() + "\n";
			}
			if (gameOver) {
				int lowestScore = 0;
				for (int i = 0; i < players.length; ++i) {
					if (players[i].getPoints() < players[lowestScore].getPoints()) {
						lowestScore = i;
					}
				}
				if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(frame,
						"The game is over and " + players[lowestScore].name + " has won!\n\n" + scores + "\nAnother game?",
						"Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)) {
					System.exit(0);
					return;
				}
				for (Player p : players) {
					p.reset();
				}
				dealCards();
			} else {
				JOptionPane.showMessageDialog(frame, scores, "Current Scores", JOptionPane.INFORMATION_MESSAGE);
				dealCards();
			}
		} else if (roundStarter != 0) {
			aiMoves(roundStarter);
		}
		draw();
	}
	private void aiMoves(int p) {
		draw();
		// NOCOM dummy AI
		if (roundStarter == p) {
			playing[p] = players[p].hand.remove(0);
		} else {
			for (Card c : players[p].hand) {
				if (c.color == playing[roundStarter].color) {
					playing[p] = c;
					players[p].hand.remove(c);
					break;
				}
			}
			if (playing[p] == null) {
				playing[p] = players[p].hand.remove(0);
			}
		}
		draw();
		if ((p + 1) % players.length == roundStarter) {
			endOfTrick = true;
		} else if (p + 1 != players.length) {
			aiMoves(p + 1);
		}
	}
	
	public Game() {
		
		frame = new JFrame("Heart");
		display = new JLabel();
		
		players = new Player[] {
			new Player("Benedikt"),
			new Player("West"),
			new Player("North"),
			new Player("East")
		};
		playing = new Card[] { null, null, null, null };
		endOfTrick = false;
		
		cardRects = new ArrayList<>();
		
		display.setPreferredSize(new Dimension(800, 600));
		
		frame.add(display);
		display.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				
				
				draw();
			}
		});
		display.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent m) {
				if (endOfTrick) {
					endOfTrick();
				} else if (playing[0] == null && (roundStarter == 0 || playing[3] != null)) {
					for (int i = cardRects.size() - 1; i >= 0; --i) {
						if (cardRects.get(i).contains(m.getPoint())) {
							Card card = players[0].hand.get(i);
							if (card.value != Card.Value.Two || card.color != Card.Color.Clubs) {
								if (roundStarter == 0) {
									if (card.color == Card.Color.Hearts) {
										boolean haveOtherThanHearts = false;
										for (Card c : players[0].hand) {
											if (c.color != Card.Color.Hearts) {
												haveOtherThanHearts = true;
												break;
											}
										}
										if (haveOtherThanHearts) {
											int heartsLeftTotal = 0;
											for (Player p : players) {
												for (Card c : p.hand) {
													if (c.color == Card.Color.Hearts) {
														++heartsLeftTotal;
													}
												}
											}
											if (heartsLeftTotal > 12) {
												continue;  // NOT ALLOWED to play hearts before hearts are broken when we have non-heart cards
											}
										}
									}
								} else {
									if (card.color != playing[roundStarter].color) {
										boolean haveLeading = false;
										for (Card c : players[0].hand) {
											if (c.color == playing[roundStarter].color) {
												haveLeading = true;
												break;
											}
										}
										if (haveLeading) {
											continue;  // NOT ALLOWED to play another color if we have the leading color
										}
										if ((playing[roundStarter].value == Card.Value.Two && playing[roundStarter].color == Card.Color.Clubs) &&
												(card.color == Card.Color.Hearts || (card.color == Card.Color.Spades && card.value == Card.Value.Queen))) {
											continue;  // NOT ALLOWED to play point cards in the first round
										}
									}
								}
							}
							playing[0] = card;
							players[0].hand.remove(card);
							if (playing[1] != null) {
								endOfTrick = true;
							} else {
								aiMoves(1);
							}
							break;
						}
					}
				}
				draw();
			}
		});
		display.addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent m) {
				
			}
		});
		display.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				draw();
			}
		});
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		display.setFocusable(true);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		dealCards();
	}

	public static void main(String[] args) {
		new Game();
	}
	
}
