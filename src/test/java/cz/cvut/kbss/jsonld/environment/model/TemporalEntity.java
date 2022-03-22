package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.time.*;
import java.util.Date;

@OWLClass(iri = Vocabulary.DEFAULT_PREFIX + "temporal-entity")
public class TemporalEntity {

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "local-date-time")
    private LocalDateTime localDateTime;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "offset-date-time")
    private OffsetDateTime offsetDateTime;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "zoned-date-time")
    private ZonedDateTime zonedDateTime;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "instant")
    private Instant instant;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "timestamp")
    private Date timestamp;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "local-date")
    private LocalDate localDate;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "offset-time")
    private OffsetTime offsetTime;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "local-time")
    private LocalTime localTime;

    public void initTemporalValues() {
        this.offsetDateTime = OffsetDateTime.now();
        this.localDateTime = offsetDateTime.toLocalDateTime();
        this.zonedDateTime = offsetDateTime.toZonedDateTime();
        this.instant = offsetDateTime.toInstant();
        this.timestamp = Date.from(instant);
        this.localDate = offsetDateTime.toLocalDate();
        this.offsetTime = offsetDateTime.toOffsetTime();
        this.localTime = offsetTime.toLocalTime();
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    public void setZonedDateTime(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public OffsetTime getOffsetTime() {
        return offsetTime;
    }

    public void setOffsetTime(OffsetTime offsetTime) {
        this.offsetTime = offsetTime;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    @Override
    public String toString() {
        return "TemporalEntity{" + "localDateTime=" + localDateTime + ", offsetDateTime=" + offsetDateTime + ", zonedDateTime=" + zonedDateTime + ", instant=" + instant + ", timestamp=" + timestamp + ", localDate=" + localDate + ", offsetTime=" + offsetTime + ", localTime=" + localTime + '}';
    }
}
