import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


class DialogQuery extends Dialog implements WindowListener {

    java.awt.TextArea textArea;
    String sqlQuery;

    void buttonCancel_Clicked() {
        this.dispose();
    }

    void buttonOK_Clicked() {
        sqlQuery = textArea.getText();
        this.dispose();
    }


    public DialogQuery(Frame parent, boolean modal, String query) {
        super(parent, modal);
        addNotify();
        resize(insets().left + insets().right + 600, insets().top + insets().bottom + 400);
        setBackground(Color.LIGHT_GRAY);

        setLayout(new BorderLayout(0,5));
        Label label1 = new java.awt.Label("Query text:");
        add(label1, BorderLayout.NORTH);
        textArea = new java.awt.TextArea();
        add(textArea, BorderLayout.CENTER);
        textArea.setText(query);

        Panel panel = new Panel(new FlowLayout(FlowLayout.CENTER,10,0));
        java.awt.Button buttonOK = new java.awt.Button("    OK    ");
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonOK_Clicked();
            }
        });

        java.awt.Button buttonCancel = new java.awt.Button("Cancel");
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonCancel_Clicked();
            }
        });

        panel.add(buttonOK);
        panel.add(buttonCancel);
        add(panel, BorderLayout.SOUTH);

        setTitle("Enter a query statement");
        addWindowListener(this);
    }

    public void show() {
        Rectangle bounds = getParent().bounds();
        Rectangle abounds = bounds();
        move(bounds.x + (bounds.width - abounds.width) / 2, bounds.y + (bounds.height - abounds.height) / 2);
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
