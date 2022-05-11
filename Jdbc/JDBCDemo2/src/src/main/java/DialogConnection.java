/*
 *  DialogConnection.java
 *
 *  $Id$
 *
 *  Sample JDBC program
 *
 *  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
 *  project.
 *
 *  Copyright (C) 1998-2022 OpenLink Software
 *
 *  This project is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation; only version 2 of the License, dated June 1991.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 *
 *
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class DialogConnection extends Dialog implements WindowListener {
    java.awt.TextField textURL, textDriver;
    String sURL;
    String sDriver;

    void buttonCancel_Clicked() {
        this.dispose();
    }

    void buttonOK_Clicked() {
        sURL = textURL.getText();
        sDriver = textDriver.getText();
        this.dispose();
    }


    public DialogConnection(Frame parent, boolean modal, String urlText, String driverText, boolean bShowDriver) {
        super(parent, modal);
        addNotify();
        resize(insets().left + insets().right + 350, insets().top + insets().bottom + 150);
        setBackground(new Color(12632256));
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);

        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        java.awt.Label label2 = new java.awt.Label("Driver Name :");
        gbl.setConstraints(label2, gbc);
        add(label2);

        textDriver = new java.awt.TextField();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(textDriver, gbc);
        add(textDriver);

        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        java.awt.Label label1 = new java.awt.Label("Connection URL :");
        gbl.setConstraints(label1, gbc);
        add(label1);

        textURL = new java.awt.TextField();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(textURL, gbc);
        add(textURL);


        java.awt.Panel panel3 = new java.awt.Panel();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbl.setConstraints(panel3, gbc);
        add(panel3);

        gbc.gridx = GridBagConstraints.RELATIVE;
        java.awt.Button buttonOK = new java.awt.Button("OK");
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonOK_Clicked();
            }
        });
        gbl.setConstraints(buttonOK, gbc);
        add(buttonOK);

        java.awt.Button buttonCancel = new java.awt.Button("Cancel");
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonCancel_Clicked();
            }
        });
        gbl.setConstraints(buttonCancel, gbc);
        add(buttonCancel);
        setTitle("Connection");
        textDriver.setText(driverText);
        textURL.setText(urlText);
        addWindowListener(this);
    }


    public void show() {
        Rectangle bounds = getParent().bounds();
        Rectangle abounds = bounds();

        move(bounds.x + (bounds.width - abounds.width) / 2,
                bounds.y + (bounds.height - abounds.height) / 2);
        super.show();
    }


    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        dispose();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
