package org.manjot.commands.utility;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.manjot.Mongo;
import org.manjot.Utils;
import org.manjot.commandhandler.Command;
import org.manjot.commandhandler.CommandListener;
import org.manjot.commandhandler.CommandType;

import com.mongodb.client.MongoCollection;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RemindCommand extends Command implements CommandListener {

    public RemindCommand() {
        this.setName("remind")
                .setAliases("remindme")
                .setDescription("Schedules a reminder for you")
                .setUsage("remind <unix timestamp> <message>")
                .setType(CommandType.UTILITY);
    }

    @Override
    public void onCommand(JDA jda, MessageReceivedEvent event, User author, Member member, Guild guild,
            MessageChannel channel, Message message, String prefix, String[] args) {
        if (args.length < 2) {
            replyInvalidUsage(message);
            return;
        }
        long unixTimestamp = Long.parseLong(args[0]);
        if (unixTimestamp < Instant.now().getEpochSecond()) {
            message.reply("You can't set a reminder in the past!").queue();
            return;
        }
        MongoCollection<Document> reminderDB = Mongo.client.getDatabase("reminders")
                .getCollection("users");
        MongoCollection<Document> counter = Mongo.client.getDatabase("reminders")
                .getCollection("counter");
        Document count = counter.find().first();
        int countInt;
        if (count == null) {
            counter.insertOne(new Document("count", 1));
            countInt = 1;
        } else {
            counter.updateOne(new Document("count", count.getInteger("count")),
                    new Document("$set", new Document("count", count.getInteger("count") + 1)));
            countInt = count.getInteger("count");
        }
        String remindMessage = Utils.messageFrom(args, 1);

        Document reminder = new Document("reminder_id", countInt)
                .append("user_id", author.getId())
                .append("timestamp", unixTimestamp)
                .append("message", remindMessage);
        reminderDB.insertOne(reminder);

        String time = Utils.formatMilli((unixTimestamp - Instant.now().getEpochSecond()) * 1000);
        message.reply("Reminder set to `" + remindMessage + "` scheduled after `" + time + "`").queue();
        message.reply("Your reminder for " + remindMessage + " is ready!").queueAfter(unixTimestamp - Instant.now().getEpochSecond(), TimeUnit.SECONDS);
    }
}
