package com.starexchangealliance.shared.utils.tests;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@Getter
@AllArgsConstructor
public class FrameworkTestParameters {

    private final File folderScenarios;
    private final File folderConfigs;
    private final File folderCredentials;
    private final File folderPatches;

    private final String fileNamePatchDefault;

}
