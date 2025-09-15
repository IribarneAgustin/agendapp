package com.agendapp.api.service;

import com.agendapp.api.controller.request.OfferingRequest;
import com.agendapp.api.controller.response.OfferingResponse;
import com.agendapp.api.entity.Offering;
import com.agendapp.api.repository.OfferingRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OfferingServiceImpl implements OfferingService {

    private final OfferingRepository offeringRepository;
    private final ModelMapper modelMapper;

    public OfferingServiceImpl(OfferingRepository offeringRepository, ModelMapper modelMapper) {
        this.offeringRepository = offeringRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public OfferingResponse create(OfferingRequest offeringRequest) {
        Offering offering = modelMapper.map(offeringRequest, Offering.class);
        return modelMapper.map(offeringRepository.save(offering), OfferingResponse.class);
    }

    @Override
    public OfferingResponse update(OfferingRequest offeringRequest) {
        if (offeringRequest.getId() == null) {
            throw new IllegalArgumentException("Invalid offering id");
        }
        Offering existing = offeringRepository.findById(offeringRequest.getId().toString())
                .orElseThrow(() -> new IllegalArgumentException("Offering not found with id: " + offeringRequest.getId()));

        modelMapper.map(offeringRequest, existing);

        Offering updated = offeringRepository.save(existing);

        return modelMapper.map(updated, OfferingResponse.class);
    }

    @Override
    public List<OfferingResponse> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user id");
        }
        List<Offering> offerings = offeringRepository.findByUserId(userId.toString());
        return offerings.stream()
                .map(off -> modelMapper.map(off, OfferingResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid offering id");
        }
        Offering existing = offeringRepository.findById(id.toString())
                .orElseThrow(() -> new IllegalArgumentException("Offering not found with id: " + id));
        offeringRepository.delete(existing);
    }

}
