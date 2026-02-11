package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, String> {

}
