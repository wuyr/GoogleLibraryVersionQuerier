package com.wuyr.google_library_query;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VersionSelectorDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonCancel;
    private JList<String> list;

    public VersionSelectorDialog(DefaultListModel<String> dataList, OnSelectedListener onSelectedListener) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonCancel);

        list.setModel(dataList);
        list.addListSelectionListener(listSelectionEvent -> {
            if (!listSelectionEvent.getValueIsAdjusting()) {
                onSelectedListener.onItemSelected(list.getSelectedValue());
                dispose();
            }
        });

        buttonCancel.addActionListener(e -> dispose());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    public static void show(DefaultListModel<String> dataList, OnSelectedListener onSelectedListener) {
        VersionSelectorDialog dialog = new VersionSelectorDialog(dataList, onSelectedListener);
        dialog.pack();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = (screenSize.width - dialog.getWidth()) / 2;
        final int y = (screenSize.height - dialog.getHeight()) / 2;
        dialog.setLocation(x, y);
        dialog.setVisible(true);
    }

    public interface OnSelectedListener {
        void onItemSelected(String item);
    }

}
