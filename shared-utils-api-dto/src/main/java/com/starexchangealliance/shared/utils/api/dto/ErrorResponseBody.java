package com.starexchangealliance.shared.utils.api.dto;

import com.starexchangealliance.shared.utils.api.dto.error.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponseBody {

    private List<Message> issues;

}
