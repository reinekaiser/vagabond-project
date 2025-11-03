package com.ie207.vagabond.config;

import com.ie207.vagabond.model.Tour;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class TourModelListener extends AbstractMongoEventListener<Tour> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Tour> event) {
        Tour tour = event.getSource();
        if (tour.getDuration() != null) {
            tour.calculateDurationInHours();
        }
        super.onBeforeConvert(event);
    }
}
