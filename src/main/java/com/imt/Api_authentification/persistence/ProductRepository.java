package com.imt.Api_authentification.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.imt.Api_authentification.persistence.dao.ProductMongoDao;
import com.imt.Api_authentification.persistence.dto.ProductMongoDto;
import com.imt.Api_authentification.service.port.ProductPort;

import static java.util.UUID.randomUUID;

@Component
@RequiredArgsConstructor
public class ProductRepository implements ProductPort {

    private final ProductMongoDao productMongoDao;

    public String save(String name, double price) {
        var productMongoDto = new ProductMongoDto(
                randomUUID(),
                name,
                price
        );

        ProductMongoDto savedProductDto = productMongoDao.save(productMongoDto);

        return savedProductDto.getId().toString();
    }

}
