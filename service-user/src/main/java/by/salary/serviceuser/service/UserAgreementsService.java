package by.salary.serviceuser.service;

import by.salary.serviceuser.entities.Permission;
import by.salary.serviceuser.entities.PermissionsEnum;
import by.salary.serviceuser.entities.User;
import by.salary.serviceuser.entities.UserAgreement;
import by.salary.serviceuser.exceptions.CustomValidationException;
import by.salary.serviceuser.exceptions.NotEnoughtPermissionsException;
import by.salary.serviceuser.exceptions.UserNotFoundException;
import by.salary.serviceuser.model.*;
import by.salary.serviceuser.model.user.agreement.UserAgreementRequestDTO;
import by.salary.serviceuser.model.user.agreement.UserAgreementResponseDTO;
import by.salary.serviceuser.repository.UserAgreementsRepository;
import by.salary.serviceuser.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.ValidationException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Stream;

@Service
public class UserAgreementsService {

    private final UserAgreementsRepository userAgreementsRepository;
    private final UserRepository userRepository;

    private WebClient.Builder webClientBuilder;

    @Autowired
    public UserAgreementsService(UserAgreementsRepository userAgreementsRepository, UserRepository userRepository, WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        this.userAgreementsRepository = userAgreementsRepository;
        this.userRepository = userRepository;
    }


    public List<UserAgreementResponseDTO> getAllUserAgreements(BigInteger userId, String email) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with id " + userId + " not found", HttpStatus.NOT_FOUND);
        }

        if (!userId.equals(userRepository.findByUserEmail(email).get().getId())
                & !Permission.isPermitted(userRepository.findByUserEmail(email).get(), PermissionsEnum.CRUD_USER_AGREEMENT)
        ) {
            throw new NotEnoughtPermissionsException("User with id " + userId + " not found", HttpStatus.FORBIDDEN);
        }

        List<UserAgreementResponseDTO> userAgreements = new ArrayList<>();
        userAgreementsRepository.findAll().forEach(userAgreement -> {
            if (userAgreement.getUser().getId().equals(userId)) {
                userAgreements.add(new UserAgreementResponseDTO(userAgreement));
            }
        });


        return userAgreements;
    }

    public List<UserAgreementResponseDTO> getAllUserAgreements(BigInteger userId, String email,
                                                               SelectionCriteriaDto selectionCriteriaDto) {

        Optional<User> user = userRepository.findById(userId);

        SelectionCriteria selectionCriteria = null;
        try {
            selectionCriteria = mapToSelectionCriteria(selectionCriteriaDto);
        }catch (IllegalArgumentException e){
            throw new ValidationException("Wrong selection criteria");
        }
        if (user.isEmpty()) {
            throw new UserNotFoundException("User with id " + userId + " not found", HttpStatus.NOT_FOUND);
        }

        if (!user.get().getUserEmail().equals(email)
                & !Permission.isPermitted(user.get(), PermissionsEnum.CRUD_USER_AGREEMENT)
        ) {
            throw new NotEnoughtPermissionsException("User with id " + userId + " not found", HttpStatus.FORBIDDEN);
        }

        List<UserAgreementResponseDTO> userAgreements = new ArrayList<>();
        try {
            userAgreementsRepository.findWithCriteria(selectionCriteria).forEach(userAgreement -> {
                if (userAgreement.getUser().getId().equals(userId)) {
                    userAgreements.add(new UserAgreementResponseDTO(userAgreement));
                }
            });
        } catch (ParseException e) {
            throw new CustomValidationException("Wrong selection criteria");
        }

        return userAgreements;
    }

    public void deleteUserAgreement(BigInteger agreementId, String email) {

        Optional<User> optUser = userRepository.findByUserEmail(email);
        if (optUser.isEmpty()) {
            throw new UserNotFoundException("User with email " + email + " not found", HttpStatus.NOT_FOUND);
        }

        if (!Permission.isPermitted(optUser.get(), PermissionsEnum.CRUD_USER_AGREEMENT)) {
            throw new NotEnoughtPermissionsException("You have not enought permissions to perform this action", HttpStatus.FORBIDDEN);
        }
        if (!userRepository.existsByUserEmail(email)) {
            throw new UserNotFoundException("User with email " + email + " not found", HttpStatus.NOT_FOUND);
        }
        if (!userAgreementsRepository.existsById(agreementId)) {
            throw new UserNotFoundException("User agreement with id " + agreementId + " not found", HttpStatus.NOT_FOUND);
        }
        userAgreementsRepository.deleteById(agreementId);
    }


    public UserAgreementResponseDTO createUserAgreement(UserAgreementRequestDTO userAgreementRequestDTO, BigInteger userId, String email) {
        Optional<User> optUser = userRepository.findByUserEmail(email);
        if (optUser.isEmpty()) {
            throw new UserNotFoundException("User with email " + email + " not found", HttpStatus.NOT_FOUND);
        }
        if (!Permission.isPermitted(optUser.get(), PermissionsEnum.CRUD_USER_AGREEMENT)) {
            throw new NotEnoughtPermissionsException("You have not enought permissions to perform this action", HttpStatus.FORBIDDEN);
        }
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with id " + userId + " not found", HttpStatus.NOT_FOUND);
        }
        UserAgreement userAgreement = new UserAgreement(
                userAgreementRequestDTO,
                userRepository.findById(userId).get(),
                optUser.get(),
                getAgreementState(userAgreementRequestDTO.getAgreementId()));
        return new UserAgreementResponseDTO(userAgreementsRepository.save(userAgreement));
    }

    private String getAgreementState(BigInteger id) {
        return Objects.requireNonNull(webClientBuilder
                .build()
                .get()
                .uri("http://service-agreement:8080/agreements/getagreementstate/" + id)
                .retrieve()
                .bodyToMono(String.class)
                .block());

    }

    private SelectionCriteria mapToSelectionCriteria(SelectionCriteriaDto selectionCriteriaDto) {
        SelectionCriteria selectionCriteria = new SelectionCriteria();

        if (selectionCriteriaDto.getFilter() != null) {
            Map<String, Map<FilterCriteria.FilterCriteriaType, String>> filter = new HashMap<>();
            for (var filterEntry : selectionCriteriaDto.getFilter().entrySet()) {
                Map<FilterCriteria.FilterCriteriaType, String> value = new HashMap<>();
                for (var filterTypeEntry : filterEntry.getValue().entrySet()) {
                    value.put(FilterCriteria.FilterCriteriaType.valueOfString(filterTypeEntry.getKey()), filterTypeEntry.getValue());
                }
                filter.put(filterEntry.getKey(), value);
            }
            selectionCriteria.setFilter(filter);
        }

        if (selectionCriteriaDto.getOrder() != null) {
            Map<String, OrderCriteria.OrderCriteriaType> order = new LinkedHashMap<>();
            for (var orderEntry : selectionCriteriaDto.getOrder().entrySet()) {
                order.put(orderEntry.getKey(), OrderCriteria.OrderCriteriaType.valueOfString(orderEntry.getValue()));
            }
            selectionCriteria.setOrder(order);
        }

        if (selectionCriteriaDto.getPagination() != null) {
            Map<PaginationCriteria.PaginationType, String> pagination = new HashMap<>();
            for (var paginationEntry : selectionCriteriaDto.getPagination().entrySet()) {
                pagination.put(PaginationCriteria.PaginationType.valueOfString(paginationEntry.getKey()), paginationEntry.getValue());
            }
            selectionCriteria.setPagination(pagination);
        }


        return selectionCriteria;
    }
}
