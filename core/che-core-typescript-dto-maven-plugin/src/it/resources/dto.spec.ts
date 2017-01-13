/*
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
import {org} from './dto';

let expect = require('chai').expect;

class DTOBuilder {

    static MY_CUSTOM_NAME : string = "myCustomName";
    static MY_CUSTOM_STATUS : string  = "myCustomStatus";
    static MY_CUSTOM_MAP_ENTRY_NAME : string = "myEntry";

    static MY_OTHER_NAME : string = "myOtherName";

    static MY_SIMPLE_ID : number = 2503;
    static MY_SIMPLE_BOOLEAN : boolean = true;
    static MY_SIMPLE_DOUBLE : number = 19.79;


    static buildSimpleDto() : org.eclipse.che.plugin.typescript.dto.MySimpleDTO {
        let mySimpleDTO : org.eclipse.che.plugin.typescript.dto.MySimpleDTO = new org.eclipse.che.plugin.typescript.dto.MySimpleDTOImpl();
        mySimpleDTO.withId(DTOBuilder.MY_SIMPLE_ID).withBoolean(DTOBuilder.MY_SIMPLE_BOOLEAN).withDouble(DTOBuilder.MY_SIMPLE_DOUBLE);
        return mySimpleDTO;
    }

    static buildCustomDto() : org.eclipse.che.plugin.typescript.dto.MyCustomDTO {
        let myCustomDTO : org.eclipse.che.plugin.typescript.dto.MyCustomDTO = new org.eclipse.che.plugin.typescript.dto.MyCustomDTOImpl();
        myCustomDTO.withName(DTOBuilder.MY_CUSTOM_NAME).withStatus(DTOBuilder.MY_CUSTOM_STATUS).withConfig(DTOBuilder.buildConfigDTO());
        myCustomDTO.getCustomMap().set(DTOBuilder.MY_CUSTOM_MAP_ENTRY_NAME, DTOBuilder.buildConfigDTO());
        return myCustomDTO;
    }

    static buildConfigDTO() : org.eclipse.che.plugin.typescript.dto.MyOtherDTO {
        let configDTO : org.eclipse.che.plugin.typescript.dto.MyOtherDTO = new org.eclipse.che.plugin.typescript.dto.MyOtherDTOImpl();
        configDTO.withName(DTOBuilder.MY_OTHER_NAME);
        return configDTO;
    }

}

describe("DTO serialization tests", () => {


    it("check simple DTO implementation", () => {

        let myCustomDTO : org.eclipse.che.plugin.typescript.dto.MySimpleDTO = DTOBuilder.buildSimpleDto();
        expect(myCustomDTO.getId()).to.eql(DTOBuilder.MY_SIMPLE_ID);
        expect(myCustomDTO.getBoolean()).to.eql(DTOBuilder.MY_SIMPLE_BOOLEAN);
        expect(myCustomDTO.getDouble()).to.eql(DTOBuilder.MY_SIMPLE_DOUBLE);

    });


    it("check build DTO implementation", () => {

        let myCustomDTO : org.eclipse.che.plugin.typescript.dto.MyCustomDTO = DTOBuilder.buildCustomDto();
        expect(myCustomDTO.getName()).to.eql(DTOBuilder.MY_CUSTOM_NAME);
        expect(myCustomDTO.getStatus()).to.eql(DTOBuilder.MY_CUSTOM_STATUS);
        expect(myCustomDTO.getConfig()).to.exist;
        expect(myCustomDTO.getConfig().getName()).to.eql(DTOBuilder.MY_OTHER_NAME);
        expect(myCustomDTO.getCustomMap().get(DTOBuilder.MY_CUSTOM_MAP_ENTRY_NAME)).to.exist;
        expect(myCustomDTO.getCustomMap().get(DTOBuilder.MY_CUSTOM_MAP_ENTRY_NAME).getName()).to.eql(DTOBuilder.MY_OTHER_NAME);

    });


    it("check build DTO implementation", () => {

        let myCustomDTO : org.eclipse.che.plugin.typescript.dto.MyCustomDTO = DTOBuilder.buildCustomDto();
        expect(myCustomDTO.getName()).to.eql(DTOBuilder.MY_CUSTOM_NAME);
        expect(myCustomDTO.getStatus()).to.eql(DTOBuilder.MY_CUSTOM_STATUS);
        expect(myCustomDTO.getConfig()).to.exist;
        expect(myCustomDTO.getConfig().getName()).to.eql(DTOBuilder.MY_OTHER_NAME);
        expect(myCustomDTO.getCustomMap().get(DTOBuilder.MY_CUSTOM_MAP_ENTRY_NAME)).to.exist;
        expect(myCustomDTO.getCustomMap().get(DTOBuilder.MY_CUSTOM_MAP_ENTRY_NAME).getName()).to.eql(DTOBuilder.MY_OTHER_NAME);

    });


    it("check build DTO implementation from source", () => {
        let myCustomDTO : org.eclipse.che.plugin.typescript.dto.MyCustomDTO = DTOBuilder.buildCustomDto();

        // build it from generated output
        let myCustomDTOFromSource : org.eclipse.che.plugin.typescript.dto.MyCustomDTO = new org.eclipse.che.plugin.typescript.dto.MyCustomDTOImpl(myCustomDTO.toJson());

        expect(myCustomDTO.getName()).to.eql(myCustomDTOFromSource.getName());
        expect(myCustomDTOFromSource.getConfig()).to.exist;
        expect(myCustomDTO.getConfig().getName()).to.eql(myCustomDTOFromSource.getConfig().getName());
        expect(myCustomDTOFromSource.getCustomMap().get(DTOBuilder.MY_CUSTOM_MAP_ENTRY_NAME)).to.exist;
        expect(myCustomDTO.getCustomMap().get(DTOBuilder.MY_CUSTOM_MAP_ENTRY_NAME).getName()).to.eql(myCustomDTOFromSource.getCustomMap().get(DTOBuilder.MY_CUSTOM_MAP_ENTRY_NAME).getName());

        expect(myCustomDTO.toJson()).to.eql(myCustomDTOFromSource.toJson());
    });




});
