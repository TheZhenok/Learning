package spring.learn;

import org.jetbrains.annotations.NotNull;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class UtilsController {
    static Map<String, String> getErrorMap(BindingResult bindingResult) {
        Collector<FieldError, ?, Map<String, String>> fieldErrorMapCollector = Collectors.toMap(
                fieldError -> fieldError.getField() + "Error",
                FieldError::getDefaultMessage
        );
        Map<String,String> errorMap = bindingResult.getFieldErrors().stream().collect(fieldErrorMapCollector);
        return errorMap;
    }
}
