package com.example.demo.service;

import com.example.demo.config.BotConfig;
import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import com.spire.doc.Section;
import com.spire.doc.documents.BuiltinStyle;
import com.spire.doc.documents.Paragraph;
import com.spire.doc.documents.ParagraphStyle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;
    private int active = 0;

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageGet = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            switch (messageGet) {
                case "/start" -> start(chatId, update.getMessage().getChat().getFirstName());
                case "/show" -> {
                    try {
                        showUsers(chatId);
                    } catch (IOException e) {
                        sendMessage(chatId, "now 'show' don't work");
                    }
                }
                case "/add" -> {
                    sendMessage(chatId, "Pls, enter the name user which you want add to the database - ");
                    active = 1;
                }
                case "/help" -> {
                    help(chatId);
                }
                case "/delete_name" -> {
                    sendMessage(chatId, "Pls, enter the name user which you want add to the database -");
                    active = 2;
                }
                case "/export" -> {
                    try {
                        sendMessage(chatId, "I send a file with the names of all users");
                        createDocumentAndSendAboutUsers(chatId);
                    } catch (IOException e) {
                        sendMessage(chatId, "now 'export' don't work");
                    }
                }
                default -> checkActive(chatId, messageGet);
            }
        }
    }

    private void checkActive(Long chatId, String messageGet) {
        switch (active) {
            case 1 -> {
                try {
                    addUser(chatId, messageGet);
                    active = 0;
                } catch (IOException e) {
                    sendMessage(chatId, "Pls, write username in one word");
                }
            }
            case 2 -> {
                try {
                    deleteUserByName(chatId, messageGet);
                    active = 0;
                } catch (IOException e) {
                    sendMessage(chatId, "Pls, write name");
                }
            }
            case 3 -> {
                try {
                    deleteUserById(chatId, messageGet);
                    active = 0;
                } catch (IOException e) {
                    sendMessage(chatId, "Pls, write id");
                }
            }
            default -> sendMessage(chatId, "I do not understand you. Use /help");
        }
    }

    private void start(Long chatId, String name) {
        String messageSend = "Hi honey, " + name + " (〃￣︶￣)人(￣︶￣〃) \n" +
                "use /help for looking what I can";
        sendMessage(chatId, messageSend);
    }

    private void help(Long chatId) {
        String messageSend =
                "You can use these commands : \n\n" +
                        "   /show   - show all users in db \n" +
                        "   /add    - add new user in db \n" +
                        "   /delete_name - delete user by name from db \n";
        sendMessage(chatId, messageSend);
    }

    private void showUsers(Long chatId) throws IOException {
        sendMessage(chatId, getUserRequestToServer());
    }

    private void addUser(Long chatId, String name) throws IOException {
        String url = "http://chserv.ddns.net:8080/create/" + name;
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        sendMessage(chatId, response.toString());
    }

    /* DON'T WORK */
    private void deleteUserById(Long chatId, String deleteId) throws IOException {
        String url = "http://chserv.ddns.net:8080/delete/" + deleteId;
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        sendMessage(chatId, response.toString());
    }

    private void deleteUserByName(Long chatId, String deleteName) throws IOException {
        String url = "http://chserv.ddns.net:8080/deletename/" + deleteName;
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        sendMessage(chatId, response.toString());
    }

    private String getUserRequestToServer() throws IOException {
        String url = "http://chserv.ddns.net:8080/show";
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    private void createDocumentAndSendAboutUsers(Long chatId) throws IOException {
        /*  Create document docs*/
        Document document = new Document();
        Section section = document.addSection();
        Paragraph subheading_1 = section.addParagraph();
        subheading_1.appendText("List all users");
        Paragraph para_1 = section.addParagraph();
        para_1.appendText(getUserRequestToServer());
        subheading_1.applyStyle(BuiltinStyle.Heading_3);
        ParagraphStyle style = new ParagraphStyle(document);
        style.setName("paraStyle");
        style.getCharacterFormat().setFontName("Arial");
        style.getCharacterFormat().setFontSize(11f);
        document.getStyles().add(style);
        para_1.applyStyle("paraStyle");
        document.saveToFile("./word.docx", FileFormat.Docx);
        InputFile inputFile = new InputFile("/home/dach/IdeaProjects/TelegramBotWithSpring/word.docx");
        sendMessage(chatId, "createDocumentAndSendAboutUsers");
        sendDocument(chatId, inputFile);
    }

    private void sendDocument(Long chatId, InputFile document) {
        SendDocument sendDocument = new SendDocument(String.valueOf(chatId), document);
/*        sendDocument.setChatId(String.valueOf(chatId));
        sendDocument.setCaption("File");
        sendDocument.setDocument(document);*/


        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage(Long chatId, String textMessage) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textMessage);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
