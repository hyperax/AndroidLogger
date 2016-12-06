package sample;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class Controller {

    public TextArea logsTextArea;
    public Button listenLogButton;
    public Button convertToJsonButton;
    public TextField commandTextField;
    public TextField prefixTextField;
    public TextField webQueryTextField;

    public void handleAction(ActionEvent event) {
        if (event.getSource() == listenLogButton) {
            loadLogs();
        } else if (event.getSource() == convertToJsonButton) {
            showJson();
        }
    }

    private void loadLogs() {
        logsTextArea.clear();

        String command = commandTextField.getText();
        if (!command.contains("-d")) {
            command += " -d";
        }
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                logsTextArea.appendText(line);
                logsTextArea.appendText("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        logsTextArea.positionCaret(logsTextArea.getLength());
    }

    private void showJson() {
        int prefixLength = prefixTextField.getLength();

        String selectedText = logsTextArea.getSelectedText();
        if (selectedText.length() == 0) {
            selectedText = logsTextArea.getText();
        }
        String[] logRows = selectedText.split("\n");

        StringBuilder logs = new StringBuilder(logRows.length);

        for (String logRow : logRows) {
            if (logRow.length() >= prefixLength) {
                logs.append(logRow.substring(prefixLength));
            } else {
                logs.append(logRow);
            }
        }

        String query = webQueryTextField.getText();
        if (!query.startsWith("http://")) {
            query = "http://" + query;
        }

        copyToClipboard(logs.toString());

        try {
            query = query.replace("%s", "");
            URI uri = new URI(query);
            Desktop.getDesktop().browse(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    private void copyToClipboard(String str) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = new StringSelection(str);
        clipboard.setContents(transferable, null);
    }

}
