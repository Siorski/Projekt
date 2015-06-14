package projekt.agents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.semanticweb.owlapi.model.OWLNamedIndividual;




public class PreferencesFrame extends JDialog {
	private static final long serialVersionUID = 1L;
	public PreferencesFrame() {
		
	}
	public String[] Create(Set<OWLNamedIndividual> individuals) throws IOException {
		final String[] ret = new String[individuals.size()];
		this.setSize(800, 540); 
		this.setResizable(false);
		this.setTitle("Holiday");
		setLayout(new BorderLayout());
		JLabel background=new JLabel(new ImageIcon(getClass().getResource("/images/travel1.png")));
		this.add(background, BorderLayout.CENTER);
		background.setLayout(new GridLayout(0,1));
		
		this.setModal(true);
		//adding other component
		JButton b = new JButton("Search");
		final JPanel panel = new JPanel( ); 
		final JPanel panel2 = new JPanel();
		final JPanel panel3 = new JPanel();
		final JPanel panelPusty = new JPanel();
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		background.setBackground(Color.blue);
		panel.setLayout(new GridLayout(0,7));
		panel.setOpaque(false);
		panel2.setLayout(new GridLayout(0,7));
		panel2.setOpaque(false);
		panel3.setLayout(new GridLayout(0,7));
		panel3.setOpaque(false);
		panelPusty.setOpaque(false);
		buttonPanel.setOpaque(false);
		Iterator<OWLNamedIndividual> it = individuals.iterator();
		while(it.hasNext()){
			String[] item2 = it.next().toString().split("#");		
			String item3 = item2[1].split(">")[0];
			JCheckBox check = new JCheckBox(item3);
			check.setBorderPainted(false);
			check.setOpaque(false);
			if(item3.equals("Train") || item3.equals("Plane") || item3.equals("Bus")) {
				panel3.add(check);
			}
			else if(item3.matches(".*[^0-9].*")) {
				panel2.add(check);
			}
			else {
				panel.add(check);
			}
			//panel.setBounds(300,40 ,200,100);
		}
		b.setBounds(318, 143, 98, 27);
		background.add(new JLabel("Choose your preferences: "));
		background.add(panel2);
		background.add(new JLabel("Choose days: "));
		background.add(panel);
		background.add(new JLabel("Choose transport: "));
		background.add(panel3);
		background.add(panelPusty);
		buttonPanel.add(b);
		background.add(buttonPanel);
		b.addActionListener(new ActionListener( ) {
			@Override
			public void actionPerformed(ActionEvent ae) {
				Component[] components = panel.getComponents();
				Component[] components2 = panel2.getComponents();
				Component[] components3 = panel3.getComponents();
				int x = 0;
				for (int i = 0; i < components.length; i++) {
					JCheckBox cb = (JCheckBox)components[i];
					if (cb.isSelected()) {
						ret[x++] = cb.getText();
					}
				}
				for (int i = 0; i < components2.length; i++) {
					JCheckBox cb = (JCheckBox)components2[i];
					if (cb.isSelected()) {
						ret[x++] = cb.getText();
					}
				}
				for (int i = 0; i < components3.length; i++) {
					JCheckBox cb = (JCheckBox)components3[i];
					if (cb.isSelected()) {
						ret[x++] = cb.getText();
					}
				}
				dispose();
			}
		});
		return ret;
	}
	
}