package com.delivery.core.ports;

import com.delivery.core_model.Constraints;

public interface ConstraintEnginePort {
    boolean validateConstraints(Constraints constraints);
}
