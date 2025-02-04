package com.journal.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.journal.entity.JournalEntry;
import com.journal.entity.SentimentData;
import com.journal.entity.User;
import com.journal.enums.Sentiment;
import com.journal.repository.UserRepositoryImpl;
import com.journal.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserSchedular {

	private UserRepositoryImpl userRepository;

	private EmailService emailService;

	private KafkaTemplate<String, SentimentData> kafkaTemplate;

	public UserSchedular(UserRepositoryImpl userRepository, EmailService emailService,
	        KafkaTemplate<String, SentimentData> kafkaTemplate) {
		super();
		this.userRepository = userRepository;
		this.emailService = emailService;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Scheduled(cron = "0 0 9 ? * SUN")
	public boolean fetchUsersAndSendSAMail() {
		List<User> users = this.userRepository.getUsersForSA();
		users.stream().forEach(user -> {
			List<Sentiment> sentiments = user.getJournalEntries().stream()
			        .filter(j -> j.getDate().isAfter(LocalDateTime.now().minus(7, ChronoUnit.DAYS)))
			        .map(JournalEntry::getSentiment).toList();
			Map<Sentiment, Integer> sentimentCounts = new EnumMap<>(Sentiment.class);
			for (Sentiment sentiment : sentiments)
				if (sentiment != null)
					sentimentCounts.put(sentiment, sentimentCounts.getOrDefault(sentiment, 0) + 1);
			Sentiment mostFrequentSentiment = null;
			int maxCount = 0;
			for (Map.Entry<Sentiment, Integer> entry : sentimentCounts.entrySet())
				if (entry.getValue() > maxCount) {
					maxCount = entry.getValue();
					mostFrequentSentiment = entry.getKey();
				}
			if (mostFrequentSentiment != null) {
				SentimentData sentimentData = SentimentData.builder().email(user.getEmail())
				        .sentiment("Sentiment for last 7 days " + mostFrequentSentiment).build();
				try {
					this.kafkaTemplate.send("weekly_sentiments", sentimentData.getEmail(), sentimentData);
				} catch (Exception e) {
					log.error("Exception ", e);
					this.emailService.sendEmail(sentimentData.getEmail(), "Sentiment for previous week",
					        sentimentData.getSentiment());
					throw e;
				}
			}

		});
		return true;
	}

}
