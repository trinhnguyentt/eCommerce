package com.ecommerce.project.service;

import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressResponse;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO);

    AddressResponse getAllAddresses(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getAddressesByUser();

    AddressDTO updateAddress(AddressDTO addressDTO, Long addressId);

    AddressDTO deleteAddress(Long addressId);
}
