package telegram.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import model.Community;
import telegram.api.InlineKeyboardBuilder;
import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author illyakurochkin */

public class EditAdminListSession extends Session {

	private Community community;
	private Map<String, Long> map;
	private int page = 0;
	private int numOfMemInSes = 10;
	private Set<Long> deleted = new HashSet<Long>();

	public EditAdminListSession(KMADeadlineBot bot, long userId, Community community) {
		super(bot, userId);
		this.community = community;
		SendMessage sm = InlineKeyboardBuilder.create(userId).addButton("<-", "1").addButton("Del", "d")
				.addButton("->", "0").nextRow().build();

		sm.setText(sendList(page).toString());
		try {
			bot.execute(sm);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public EditAdminListSession(KMADeadlineBot bot, long userId, String communityName) {
		this(bot, userId, bot.communityDao.select(communityName));
	}

	private int getNumOfPages() {
		return (int) Math.ceil(community.getMemberIds().size() / (float) numOfMemInSes);
	}

	private String sendList(int num) {
		map = new HashMap<String, Long>();
		String text = "If you want to remove somebody from admin, click on its username. To undo deleting click on the username again. "
				+ "When you're done, click 'Del' \n"
				+ "List of members: " + "(page " + (num + 1) + "/" + getNumOfPages() + ")";
		Iterator<Long> iterator = community.getAdminIds().iterator();

		for (int i = 0; i < num * numOfMemInSes; i++) {
			iterator.next();
		}

		int count = 0;
		while (count < numOfMemInSes && iterator.hasNext()) {
			try {
				long ids = iterator.next();
				String username = bot.execute(new GetChat(ids)).getUserName();
				text += "\nadmin: /_" + username;
				map.put(username, ids);
			} catch (Exception e) {
				e.printStackTrace();
			}
			count++;
		}

		return text;
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if (update.hasCallbackQuery()) {
			if (update.getCallbackQuery().getData().equals("0")) {
				if (page < getNumOfPages() - 1) {
					edit(update, ++page);
				}
				return this;

			} else if (update.getCallbackQuery().getData().equals("1")) {
				if (page > 0) {
					edit(update, --page);
				}
				return this;
			} else if (update.getCallbackQuery().getData().equals("d")) {
				return new ConfirmEditAdminListSession(bot, userId, community, deleted);
			}
		}

		if (update.hasMessage() && update.getMessage().getText().startsWith("/_")) {
			String text = update.getMessage().getText().substring(2);

			if (map.containsKey(text)) {
				long id = map.get(text);
				if (community.isAdmin(id)) {
					if (deleted.contains(id)) {
						deleted.remove(id);
					} else {
						deleted.add(id);
					}
				}
			} else {
				bot.sendText(userId, "We can't find this member in out list");
			}
		}
		return this;
	}

	private void edit(Update update, int num) {
		EditMessageText editMessage = InlineKeyboardBuilder.create(userId).addButton("<-", "1").addButton("Del", "d")
				.addButton("->", "0").nextRow().buildEdit();
		editMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
		editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
		editMessage.setText(sendList(num));
		editMessage.setInlineMessageId(update.getCallbackQuery().getInlineMessageId());
		try {
			bot.execute(editMessage);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

}
