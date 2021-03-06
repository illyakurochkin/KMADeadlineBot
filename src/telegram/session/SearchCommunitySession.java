package telegram.session;

import java.util.Set;
import java.util.stream.Collectors;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import telegram.bot.KMADeadlineBot;
import telegram.session.api.Session;

/** @author dSigma */

public class SearchCommunitySession extends Session {
	
	private Set<String> communities;
	
	public SearchCommunitySession(KMADeadlineBot bot, long userId) {
		super(bot, userId);
		communities = bot.communityDao.selectNames();
		sendCommunities();
	}
	
	private void sendCommunities() {
		String text = communities.stream()
				.sorted()
				.limit(100)
				.map(name -> "/_" + name)
				.collect(Collectors.joining("\n"));
		text += "\n\n tap on community, you want to choose.";
		
		SendMessage message = new SendMessage()
				.setChatId(userId)
				.setText(text);
		
		try {
			bot.execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Session updateListener(Update update) throws TelegramApiException {
		if(update.hasMessage() && update.getMessage().hasText()) {
			String text = update.getMessage().getText().toLowerCase().substring(2);
						
			String communityName = communities.stream().filter(name -> name.toLowerCase().startsWith(text))
					.sorted().findFirst().orElse(null);
			
			if(communityName == null) {
				sendCommunities();
				return this;
				
			} else {
				return new CommunityOptionsSession(bot, userId, communityName);
				
			}
		}
		
		sendCommunities();
		return this;
	}

}
