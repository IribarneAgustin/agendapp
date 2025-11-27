package com.reservalink.api.repository.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("BOOKING")
public class BookingPaymentEntity extends Payment {

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private BookingEntity bookingEntity;

}
