package com.ronext.rpdptw.adapter.input.testfixture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronext.rpdptw.adapter.input.AdaptationResult;
import com.ronext.rpdptw.adapter.input.AdaptedCanonicalInput;
import com.ronext.rpdptw.adapter.input.ExternalInputDocument;
import com.ronext.rpdptw.adapter.input.InputAdapter;
import com.ronext.rpdptw.input.*;
import com.ronext.rpdptw.normalization.InputPath;
import com.ronext.rpdptw.normalization.InputProblem;
import com.ronext.rpdptw.normalization.InputProblemCode;
import com.ronext.rpdptw.normalization.InputRejectionReport;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public final class TestFixtureInputAdapter implements InputAdapter {

    public static final String ADAPTER_IDENTITY_VALUE = "TEST_FIXTURE_V1";
    public static final String SCHEMA_IDENTITY_VALUE = "TEST_FIXTURE_SCHEMA_V1";

    private final TestFixtureAliasPolicy aliasPolicy;
    private final ObjectMapper objectMapper;

    public TestFixtureInputAdapter() {
        this(TestFixtureAliasPolicy.STRICT);
    }

    public TestFixtureInputAdapter(TestFixtureAliasPolicy aliasPolicy) {
        this.aliasPolicy = Objects.requireNonNull(aliasPolicy, "aliasPolicy must not be null");
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(AdapterIdentity adapter) {
        return adapter != null && ADAPTER_IDENTITY_VALUE.equals(adapter.value());
    }

    @Override
    public AdaptationResult adapt(ExternalInputDocument document) {
        Objects.requireNonNull(document, "document must not be null");

        if (!supports(document.declaredAdapterIdentity())) {
            return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                    new InputProblem.Schema(
                            InputProblemCode.UNSUPPORTED_ADAPTER_OR_SOURCE,
                            new InputPath("adapter")
                    )
            )));
        }

        byte[] rawBytes = document.rawBytes();
        byte[] sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256").digest(rawBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
        RawInputDigest digest = new RawInputDigest(sha256);

        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(rawBytes);
        } catch (Exception e) {
            return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                    new InputProblem.Schema(
                            InputProblemCode.INVALID_NUMERIC_SYNTAX,
                            new InputPath("document")
                    )
            )));
        }

        if (rootNode == null || !rootNode.isObject()) {
            return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                    new InputProblem.Schema(
                            InputProblemCode.MISSING_REQUIRED_FIELD,
                            new InputPath("document")
                    )
            )));
        }

        List<String> unknownFieldsIgnored = new ArrayList<>();
        Optional<InputProblem> unknownFieldProblem = checkUnknownFields(rootNode, aliasPolicy, unknownFieldsIgnored);
        if (unknownFieldProblem.isPresent()) {
            return new AdaptationResult.Rejected(new InputRejectionReport(List.of(unknownFieldProblem.get())));
        }

        TestFixtureExternalDto dto;
        try {
            dto = objectMapper.treeToValue(rootNode, TestFixtureExternalDto.class);
        } catch (Exception e) {
            return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                    new InputProblem.Schema(
                            InputProblemCode.MISSING_REQUIRED_FIELD,
                            new InputPath("document")
                    )
            )));
        }

        // Validate plan
        if (dto.plan() == null) {
            return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                    new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("plan"))
            )));
        }
        if (dto.plan().planId() == null || dto.plan().customer() == null || dto.plan().profile() == null
                || dto.plan().profileVersion() == null || dto.plan().planStart() == null || dto.plan().planEndExclusive() == null) {
            return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                    new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("plan"))
            )));
        }

        CanonicalPlanEnvelope planEnvelope = new CanonicalPlanEnvelope(
                new ExternalPlanId(dto.plan().planId()),
                dto.plan().customer(),
                dto.plan().profile(),
                dto.plan().profileVersion(),
                Optional.ofNullable(dto.plan().preset()),
                dto.plan().planStart(),
                dto.plan().planEndExclusive()
        );

        // Validate & map vehicles
        List<CanonicalVehicleInput> vehicles = new ArrayList<>();
        if (dto.vehicles() != null) {
            for (int i = 0; i < dto.vehicles().size(); i++) {
                TestFixtureExternalDto.VehicleDto v = dto.vehicles().get(i);
                if (v == null || v.id() == null || v.sizeFeatureCode() == null) {
                    return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                            new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("vehicles[" + i + "]"))
                    )));
                }
                vehicles.add(new CanonicalVehicleInput(
                        new ExternalVehicleId(v.id()),
                        v.sizeFeatureCode(),
                        v.capabilities() != null ? v.capabilities() : Set.of(),
                        v.vehicleZoneIds() != null ? v.vehicleZoneIds() : Set.of(),
                        Optional.ofNullable(v.ownership()),
                        Optional.ofNullable(v.speedKmH()),
                        v.oneway() != null ? v.oneway() : true,
                        v.singleRoundtrip() != null ? v.singleRoundtrip() : false,
                        Optional.ofNullable(v.waitPolicy()),
                        Optional.ofNullable(v.routeResourceLimit())
                ));
            }
        }

        // Validate & map locations
        List<CanonicalLocationInput> locations = new ArrayList<>();
        if (dto.locations() != null) {
            for (int i = 0; i < dto.locations().size(); i++) {
                TestFixtureExternalDto.LocationDto l = dto.locations().get(i);
                if (l == null || l.id() == null) {
                    return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                            new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("locations[" + i + "]"))
                    )));
                }
                locations.add(new CanonicalLocationInput(
                        new ExternalLocationId(l.id()),
                        Optional.ofNullable(l.zone())
                ));
            }
        }

        // Validate & map requests
        List<CanonicalRequestInput> requests = new ArrayList<>();
        if (dto.requests() != null) {
            for (int i = 0; i < dto.requests().size(); i++) {
                TestFixtureExternalDto.RequestDto r = dto.requests().get(i);
                if (r == null || r.id() == null || r.servicePattern() == null) {
                    return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                            new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("requests[" + i + "]"))
                    )));
                }

                // Check order-level task time rejection
                if (r.taskTimeSeconds() != null || r.taskTime() != null) {
                    return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                            new InputProblem.Schema(
                                    InputProblemCode.ORDER_LEVEL_TASK_TIME_NOT_ALLOWED,
                                    new InputPath("requests[" + i + "].taskTimeSeconds")
                            )
                    )));
                }

                // Check raw customer extension rejection
                if (r.rawCustomerExtension() != null && !r.rawCustomerExtension().isNull() && !r.rawCustomerExtension().isMissingNode()) {
                    return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                            new InputProblem.Schema(
                                    InputProblemCode.UNAPPROVED_EXTENSION_INPUT,
                                    new InputPath("requests[" + i + "].rawCustomerExtension")
                            )
                    )));
                }

                ServicePattern pattern;
                try {
                    pattern = ServicePattern.valueOf(r.servicePattern());
                } catch (Exception e) {
                    return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                            new InputProblem.Schema(InputProblemCode.INVALID_SERVICE_PATTERN, new InputPath("requests[" + i + "].servicePattern"))
                    )));
                }

                if (pattern == ServicePattern.DELIVERY_ONLY && r.pickup() != null) {
                    return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                            new InputProblem.Schema(InputProblemCode.INVALID_SERVICE_PATTERN, new InputPath("requests[" + i + "].pickup"))
                    )));
                }
                if (pattern == ServicePattern.PICKUP_DELIVERY && r.pickup() == null) {
                    return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                            new InputProblem.Schema(InputProblemCode.INVALID_SERVICE_PATTERN, new InputPath("requests[" + i + "].pickup"))
                    )));
                }

                if (r.delivery() == null || r.delivery().locationId() == null || r.delivery().windowOpen() == null
                        || r.delivery().windowCloseInclusive() == null || r.delivery().durationSeconds() == null) {
                    return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                            new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("requests[" + i + "].delivery"))
                    )));
                }

                Optional<CanonicalServiceInput> pickupVisit;
                if (r.pickup() != null) {
                    if (r.pickup().locationId() == null || r.pickup().windowOpen() == null
                            || r.pickup().windowCloseInclusive() == null || r.pickup().durationSeconds() == null) {
                        return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                                new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("requests[" + i + "].pickup"))
                        )));
                    }
                    pickupVisit = Optional.of(new CanonicalServiceInput(
                            new ExternalLocationId(r.pickup().locationId()),
                            r.pickup().windowOpen(),
                            r.pickup().windowCloseInclusive(),
                            r.pickup().durationSeconds(),
                            Optional.ofNullable(r.pickup().reqDate()),
                            Optional.ofNullable(r.pickup().zone())
                    ));
                } else {
                    pickupVisit = Optional.empty();
                }

                CanonicalServiceInput deliveryVisit = new CanonicalServiceInput(
                        new ExternalLocationId(r.delivery().locationId()),
                        r.delivery().windowOpen(),
                        r.delivery().windowCloseInclusive(),
                        r.delivery().durationSeconds(),
                        Optional.ofNullable(r.delivery().reqDate()),
                        Optional.ofNullable(r.delivery().zone())
                );

                List<CanonicalItemInput> items = new ArrayList<>();
                if (r.items() != null) {
                    for (int j = 0; j < r.items().size(); j++) {
                        TestFixtureExternalDto.ItemDto item = r.items().get(j);
                        if (item == null || item.weightDecimal() == null || item.volumeDecimal() == null || item.itemTaskTimeSeconds() == null || item.quantity() == null) {
                            return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                                    new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("requests[" + i + "].items[" + j + "]"))
                            )));
                        }
                        items.add(new CanonicalItemInput(
                                item.weightDecimal(),
                                item.volumeDecimal(),
                                item.quantity(),
                                item.itemTaskTimeSeconds()
                        ));
                    }
                }

                if (r.compatibility() == null || r.compatibility().allowedVehicleSizes() == null || r.compatibility().requiredVehicleCapabilities() == null) {
                    return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                            new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("requests[" + i + "].compatibility"))
                    )));
                }

                CanonicalCompatibilityInput compat = new CanonicalCompatibilityInput(
                        r.compatibility().allowedVehicleSizes(),
                        r.compatibility().requiredVehicleCapabilities()
                );

                Optional<ApprovedTypedExtensionInput> extensionInput = Optional.empty();
                if (r.extensionInput() != null) {
                    if (r.extensionInput().typeUri() == null || r.extensionInput().payloadJson() == null) {
                        return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                                new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("requests[" + i + "].extensionInput"))
                        )));
                    }
                    extensionInput = Optional.of(new ApprovedTypedExtensionInput(
                            r.extensionInput().typeUri(),
                            r.extensionInput().payloadJson()
                    ));
                }

                requests.add(new CanonicalRequestInput(
                        new ExternalRequestId(r.id()),
                        pattern,
                        pickupVisit,
                        deliveryVisit,
                        items,
                        compat,
                        Optional.ofNullable(r.mandatoryDeclaration()),
                        extensionInput
                ));
            }
        }

        // Validate & map travelCosts
        List<CanonicalTravelInput> travelCosts = new ArrayList<>();
        if (dto.travelCosts() != null) {
            for (int i = 0; i < dto.travelCosts().size(); i++) {
                TestFixtureExternalDto.TravelCostDto tc = dto.travelCosts().get(i);
                if (tc == null || tc.from() == null || tc.to() == null || tc.durationSeconds() == null || tc.distanceMeters() == null) {
                    return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                            new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("travelCosts[" + i + "]"))
                    )));
                }
                travelCosts.add(new CanonicalTravelInput(
                        new ExternalLocationId(tc.from()),
                        new ExternalLocationId(tc.to()),
                        tc.durationSeconds(),
                        tc.distanceMeters()
                ));
            }
        }

        InputProvenance provenance = new InputProvenance(
                document.declaredAdapterIdentity(),
                new SchemaIdentity(SCHEMA_IDENTITY_VALUE),
                List.of(),
                unknownFieldsIgnored
        );

        CanonicalBusinessInput canonicalInput = new CanonicalBusinessInput(
                planEnvelope,
                vehicles,
                locations,
                requests,
                travelCosts,
                provenance
        );

        return new AdaptationResult.Accepted(new AdaptedCanonicalInput(canonicalInput, digest));
    }

    private static final Map<String, Set<String>> KNOWN_KEYS_MAP = Map.of(
            "", Set.of("plan", "vehicles", "locations", "requests", "travelCosts"),
            "plan", Set.of("planId", "customer", "profile", "profileVersion", "preset", "planStart", "planEndExclusive"),
            "vehicle", Set.of("id", "sizeFeatureCode", "capabilities", "vehicleZoneIds", "ownership", "speedKmH", "oneway", "singleRoundtrip", "waitPolicy", "routeResourceLimit"),
            "location", Set.of("id", "zone"),
            "request", Set.of("id", "servicePattern", "pickup", "delivery", "items", "compatibility", "mandatoryDeclaration", "extensionInput", "taskTimeSeconds", "taskTime", "rawCustomerExtension"),
            "serviceVisit", Set.of("locationId", "windowOpen", "windowCloseInclusive", "durationSeconds", "reqDate", "zone"),
            "item", Set.of("weightDecimal", "volumeDecimal", "quantity", "itemTaskTimeSeconds"),
            "compatibility", Set.of("allowedVehicleSizes", "requiredVehicleCapabilities"),
            "extensionInput", Set.of("typeUri", "payloadJson"),
            "travelCost", Set.of("from", "to", "durationSeconds", "distanceMeters")
    );

    private Optional<InputProblem> checkUnknownFields(JsonNode node, TestFixtureAliasPolicy policy, List<String> unknownFieldsIgnored) {
        return findUnknownField(node, "", "", policy, unknownFieldsIgnored);
    }

    private Optional<InputProblem> findUnknownField(JsonNode node, String path, String nodeType, TestFixtureAliasPolicy policy, List<String> unknownFieldsIgnored) {
        if (node == null || !node.isObject()) {
            return Optional.empty();
        }

        Set<String> allowedKeys = KNOWN_KEYS_MAP.getOrDefault(nodeType, Set.of());
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            String currentPath = path.isEmpty() ? fieldName : path + "." + fieldName;

            if (!allowedKeys.isEmpty() && !allowedKeys.contains(fieldName)) {
                if (policy == TestFixtureAliasPolicy.STRICT) {
                    return Optional.of(new InputProblem.Schema(
                            InputProblemCode.UNKNOWN_FIELD_REJECTED,
                            new InputPath(currentPath)
                    ));
                } else {
                    unknownFieldsIgnored.add(fieldName);
                }
            }

            JsonNode child = node.get(fieldName);
            if (child != null && allowedKeys.contains(fieldName) && !"rawCustomerExtension".equals(fieldName) && !"extensionInput".equals(fieldName)) {
                String childNodeType = determineNodeType(nodeType, fieldName);
                if (child.isObject()) {
                    Optional<InputProblem> subProblem = findUnknownField(child, currentPath, childNodeType, policy, unknownFieldsIgnored);
                    if (subProblem.isPresent()) return subProblem;
                } else if (child.isArray()) {
                    for (int i = 0; i < child.size(); i++) {
                        JsonNode element = child.get(i);
                        if (element.isObject()) {
                            Optional<InputProblem> subProblem = findUnknownField(element, currentPath + "[" + i + "]", childNodeType, policy, unknownFieldsIgnored);
                            if (subProblem.isPresent()) return subProblem;
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private String determineNodeType(String parentNodeType, String fieldName) {
        if ("".equals(parentNodeType)) {
            return switch (fieldName) {
                case "plan" -> "plan";
                case "vehicles" -> "vehicle";
                case "locations" -> "location";
                case "requests" -> "request";
                case "travelCosts" -> "travelCost";
                default -> "";
            };
        }
        if ("request".equals(parentNodeType)) {
            return switch (fieldName) {
                case "pickup", "delivery" -> "serviceVisit";
                case "items" -> "item";
                case "compatibility" -> "compatibility";
                case "extensionInput" -> "extensionInput";
                default -> "";
            };
        }
        return "";
    }
}
