package model;

import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Deadline implements Comparable<Deadline>{

	// data fields

	private final long id;
	private Date date;
	private String description;
	private long chatId;
	private Set<Long> messageIds;
	
	private String communityName;

	// constructors

	/** main constructor with all arguments */
	public Deadline(long id, Date date, String description, String communityName, long chatId, Set<Long> messageIds) {
		this.id = id;
		this.date = date;
		this.description = description;
		this.communityName = communityName;
		this.chatId = chatId;
		this.messageIds = messageIds;
	}

	/** creates Deadline instance with empty description */
	public Deadline(long id, Date date, String communityName, long chatId, Set<Long> messageIds) {
		this(id, date, "", communityName, chatId, messageIds);
	}

	// getters and setters

	public long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public long getChatId() {
		return chatId;
	}

	public Set<Long> getMessageIds() {
		// create a copy of messageIds
		return messageIds.stream().collect(Collectors.toSet());
	}

	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setMessageIds(Set<Long> messageIds) {
		this.messageIds = new HashSet<>();
		messageIds.forEach(this.messageIds::add);
	}
	
	public void setChatId(long chatId) {
		this.chatId = chatId;
	}

	// methods

	public Duration getTimeRemaining() {
		return Duration.ofHours(0); // TODO implement
	}

	public String getCommunityName() {
		return communityName; 
	}

	// methods from class Object

	@Override
	public boolean equals(Object object) {
		// deadlines are equals if their ids are equals
		if (object != null && object instanceof Deadline) {
			return id == ((Deadline) object).id;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return String.valueOf(id).hashCode();
	}

	@Override
	public String toString() {
		return "deadline #" + id + ", description: \"" + description + "\"";
	}

	@Override
	public int compareTo(Deadline deadline) {
		return Comparator.comparing(Deadline::getDate)
				.thenComparing(Deadline::getCommunityName)
				.thenComparing(Deadline::getDescription)
				.compare(this, deadline);
	}
}