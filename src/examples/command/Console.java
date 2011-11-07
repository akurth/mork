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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

/**
 * A window to send standard and error output of a process to.
 */

public class Console extends JDialog implements ActionListener {
    private JButton ok;
    private JTextArea area;

    /** Encapsualtes the process handling stuff */
    public Console() {
        super((JFrame) null, true /* modal */);

        Container pane;

        pane = getContentPane();
        pane.setLayout(new BorderLayout());

        ok = new JButton("ok");
        ok.addActionListener(this);
        // ok is enabled if the process has finished.
        // There has to be a better way to do this ...
        ok.setEnabled(false);
        getRootPane().setDefaultButton(ok);
        pane.add(ok, BorderLayout.SOUTH);

        area = new JTextArea();
        pane.add(new JScrollPane(area));

        pack();
        setSize(300, 300);
    }

    public void actionPerformed(ActionEvent e) {
        // ok is the only action

        setVisible(false);
        dispose();
    }

    public void execute(String cmd) {
        Thread runner;

        setTitle("executing \"" + cmd + "\"");

        runner = createRunner(cmd, area, this, ok);
        runner.start();
        // I need a new thread to execute the process because
        // setVisible blocks until the modal dialog is finished ...
        setVisible(true);
    }

    /**
     * Create a new thread to execute the specified command.
     * Enables the specified button when done.
     */
    private static Thread createRunner(
        final String cmd, final JTextArea dest,
        final JDialog dialog, final JButton okButton) {
        return new Thread() {
            @Override
            public void run() {
                Process p;
                StringBuffer exit;
                Thread log1;
                Thread log2;

                exit = new StringBuffer();
                try {
                    try {
                        p = Runtime.getRuntime().exec(cmd);
                    } catch (IOException e) {
                        dest.append("\nexecution failed: " + e.getMessage());
                        return;
                    }
                    log1 = createLogger(p.getInputStream(), dest);
                    log2 = createLogger(p.getErrorStream(), dest);
                    log1.start();
                    log2.start();
                    System.out.println("waiting");
                    try {
                        log1.join();
                        log2.join();
                        // join the logger threads before waiting for
                        // the process - otherwise the exit code can be
                        // printed before the command output
                        exit.append("" + p.waitFor());
                    } catch (InterruptedException e) {
                        exit.append("interrupted");
                    }
                } finally {
                    dest.append("\nfinished, exit= " + exit.toString());
                    okButton.setEnabled(true);
                    System.out.println("launcher done");
                }
            }
        };
    }

    private static Thread createLogger(final InputStream src, final JTextArea dest) {
        return new Thread() {
            @Override
            public void run() {
                int len;
                byte[] buffer;

                buffer = new byte[256];
                while (true) {
                    try {
                        len = src.read(buffer);
                        if (len == -1) {
                            return; // end of file, thread ends
                        }
                        dest.append(new String(buffer, 0, len));
                    } catch (IOException e) {
                        System.out.println("failed: " + e);
                        return;
                    }
                    yield();
                }
            }
        };
    }
}
