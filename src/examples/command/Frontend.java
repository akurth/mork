/*
 * Copyright 1&1 Internet AG, http://www.1and1.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package command;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Frontend extends JDialog implements ActionListener {
    // used by run, okAction and cancelAction
    private boolean result;

    private JLabel[] labels;
    private JTextField[] fields;
    private JButton ok;
    private JButton cancel;

    /** size: number of files. */
    public Frontend(String title, String desc, int size) {
        super((JFrame) null, /* modal: */ true);

        int i;
        JPanel container;
        JPanel buttons;
        JPanel content;

        setTitle(title);
        container = new JPanel();
        container.setLayout(new BorderLayout(5, 5));

        container.add(BorderLayout.NORTH, new JLabel(desc, SwingConstants.CENTER));

        buttons = new JPanel();
        ok = new JButton("Ok");
        ok.addActionListener(this);
        buttons.add(ok);
        cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        buttons.add(cancel);
        container.add(BorderLayout.SOUTH, buttons);
        getRootPane().setDefaultButton(ok);

        content = new JPanel();
        content.setLayout(new GridLayout(size, 2));
        labels = new JLabel[size];
        fields = new JTextField[size];
        for (i = 0; i < fields.length; i++) {
            labels[i] = new JLabel();
            fields[i] = new JTextField(15);
            content.add(labels[i]);
            content.add(fields[i]);
        }
        container.add(BorderLayout.CENTER, content);

        container.add(BorderLayout.EAST, new JLabel(" "));
        container.add(BorderLayout.WEST, new JLabel(" "));

        getContentPane().add(container);
        pack();
    }

    public void setLabel(int field, String label) {
        labels[field].setText(label);
    }

    public boolean run() {
        setVisible(true);
        return result;
    }

    public String getValue(int field) {
        return fields[field].getText();
    }

    public void actionPerformed(ActionEvent e) {
        result = (e.getSource() == ok);
        setVisible(false);
        dispose();
    }
}
