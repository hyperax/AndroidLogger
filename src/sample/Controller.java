package sample;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class Controller {

    public TextArea logsTextArea;
    public Button listenLogButton;
    public Button goToButton;
    public Button clipboardButton;
    public TextField commandTextField;
    public TextField prefixTextField;
    public TextField webQueryTextField;

    public void handleAction(ActionEvent event) {
        if (event.getSource() == listenLogButton) {
            loadLogs();
        } else if (event.getSource() == goToButton) {
            copyToClipboard(extractTargetText());
            goToWeb();
        } else if (event.getSource() == clipboardButton) {
            copyToClipboard(extractTargetText());
        }
    }

    private void goToWeb() {
        String query = webQueryTextField.getText();
        if (!query.startsWith("http://")) {
            query = "http://" + query;
        }

        try {
            query = query.replace("%s", "");
            URI uri = new URI(query);
            Desktop.getDesktop().browse(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
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
                if (!line.isEmpty()) {
                    logsTextArea.appendText(line);
                    logsTextArea.appendText("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        logsTextArea.positionCaret(logsTextArea.getLength());
    }

    private String extractTargetText() {
        int prefixLength = prefixTextField.getLength();

        String targetText;
        String allText = logsTextArea.getText();
        if (logsTextArea.getSelectedText().isEmpty()) {
            targetText = allText;
        } else {
            IndexRange selectionRange = logsTextArea.getSelection();
            int lineStart = allText.lastIndexOf("\n", selectionRange.getStart()) + 1;
            int lineEnd = allText.indexOf("\n", selectionRange.getEnd());
            targetText = lineEnd < 0 ? allText.substring(lineStart) : allText.substring(lineStart, lineEnd);
        }
        String[] logRows = targetText.split("\n");

        StringBuilder logs = new StringBuilder(logRows.length);

        for (String logRow : logRows) {
            if (logRow.length() >= prefixLength) {
                logs.append(logRow.substring(prefixLength));
            } else {
                logs.append(logRow);
            }
        }
        return logs.toString();
    }

    private void copyToClipboard(String str) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = new StringSelection(str);
        clipboard.setContents(transferable, null);
    }

}
