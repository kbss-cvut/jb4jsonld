/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jsonld.environment.Generator;
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

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "period")
    private Period period;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "duration")
    private Duration duration;

    public void initTemporalAccessorValues() {
        this.offsetDateTime = OffsetDateTime.now();
        this.localDateTime = offsetDateTime.toLocalDateTime();
        this.zonedDateTime = offsetDateTime.toZonedDateTime();
        this.instant = offsetDateTime.toInstant();
        this.timestamp = Date.from(instant);
        this.localDate = offsetDateTime.toLocalDate();
        this.offsetTime = offsetDateTime.toOffsetTime();
        this.localTime = offsetTime.toLocalTime();
    }

    public void initTemporalAmountValues() {
        this.period =
                Period.of(Generator.randomInt(2, 100), Generator.randomInt(1, 12), Generator.randomInt(1, 28));
        this.duration = Duration.ofSeconds(Generator.randomInt(5, 10000));
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

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "TemporalEntity{" +
                "localDateTime=" + localDateTime +
                ", offsetDateTime=" + offsetDateTime +
                ", zonedDateTime=" + zonedDateTime +
                ", instant=" + instant +
                ", timestamp=" + timestamp +
                ", localDate=" + localDate +
                ", offsetTime=" + offsetTime +
                ", localTime=" + localTime +
                ", period=" + period +
                ", duration=" + duration +
                '}';
    }
}
