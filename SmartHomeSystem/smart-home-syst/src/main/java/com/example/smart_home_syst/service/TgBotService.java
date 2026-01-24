package com.example.smart_home_syst.service;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class TgBotService extends TelegramLongPollingBot{
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    private final String botUsername;
    private final String token;
    private final Long adminChatId;
    
    public TgBotService(@Value("${bot.username}") String username, @Value("${bot.token}") String token, @Value("${bot.adminChatId}") Long adminChatId) {
        this.botUsername = username;
        this.token = token;
        this.adminChatId = adminChatId;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
    @Override
    public String getBotToken() {
        return token;
    }

    public void onUpdateReceived(Update update) {
        String takenMessage = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        logger.info("Taken message {} from chat with Id {}", takenMessage, chatId.toString());
        
        if ("/start".equals(takenMessage)) {
            logger.debug("User start bot chat with Id {}", chatId.toString());
            sendMessage(chatId.toString(), "Nice to meet you! I'm Notifier Bot for Smart home system. \n\nMy main task - notify admin about different actions and send logs to him. Also I send information to User, what used this chat Id in application.\n\nIf you wanna see list of commands, you must send '/help' command list into chat");
        }
        if ("/help".equals(takenMessage)) {
            logger.debug("User ask '/help' bot chat with Id {}", chatId.toString());
            sendMessage(chatId.toString(), "Commands:\n\n'/help' - I send commands list into chat\n'/chat_id' - I send chat Id\n'/send_logs_to_admin' - If you meet the error, write this command and log file will be sended to admin");
        }
        if ("/chat_id".equals(takenMessage)) {
            logger.debug("User ask '/chat_id' bot chat with Id {}", chatId.toString());
            sendMessage(chatId.toString(), chatId.toString());
        }
        if ("/send_logs_to_admin".equals(takenMessage)) {
            logger.debug("User ask '/send_logs_to_admin' bot chat with Id {}", chatId.toString());
            sendLogsToAdmin(chatId.toString());
        }
    }

    public void sendMessage(String chatId, String text) {
        SendMessage newMessage = new SendMessage();
        newMessage.setChatId(chatId);
        newMessage.setText(text);
        try
        {
            execute(newMessage);
        }
        catch (Exception e) {
            logger.warn("Sending message error: {}", e.getMessage(), e);
        }
    }

    public void sendLogsToAdmin(String reasonChatId) {
        try {
            File pdfFile = new File("logs/smart-home-syst.log");
                
            if (!pdfFile.exists()) {
                logger.warn("Logger file doesn't founded: logs/smart-home-syst.log");
                sendMessage(adminChatId.toString(), "Logs file doesn't founded. Check!");
                sendMessage(reasonChatId, "Logs file doesn't founded. Try later(");
                return;
            }
            SendDocument currentLogs = new SendDocument();
            currentLogs.setChatId(adminChatId.toString());
            currentLogs.setDocument(new InputFile(pdfFile));
            execute(currentLogs);
            sendMessage(reasonChatId, "Logs file sended. Sorry about errors(");
            logger.info("Log file succesfully sended into chat: {}", adminChatId.toString());
        }
        catch (Exception e) {
            logger.warn("Sending file error: {}", e.getMessage(), e);
            sendMessage(adminChatId.toString(), "Sending file error: " + e.getMessage());
        }
    }
}
