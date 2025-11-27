package tetris.view.ScoreboardComponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class StandardModePanel extends JPanel {

	public StandardModePanel(JList<String> list) {
		super(new BorderLayout());
		setOpaque(false);

		JLabel label = new JLabel("STANDARD", JLabel.CENTER);
		label.setFont(new Font("SansSerif", Font.BOLD, 22));
		label.setForeground(Color.WHITE);
		label.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		add(label, BorderLayout.NORTH);

		list.setOpaque(true);
		list.setBackground(new Color(24, 24, 24));
		list.setForeground(Color.WHITE);
		list.setFont(new Font("SansSerif", Font.PLAIN, 16));
		list.setFocusable(false);

		JScrollPane scroll = new JScrollPane(list);
		scroll.getViewport().setOpaque(false);
		scroll.setOpaque(false);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		add(scroll, BorderLayout.CENTER);
	}

}
