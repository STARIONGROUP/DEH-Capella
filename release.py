import subprocess
import shutil
import os
import datetime
import xml.etree.ElementTree as ET

# Read version from feature.xml
def getVersion():
    print("Reading version from feature.xml...")
    feature_xml = os.path.join("DEHCapellaAdapterFeature/feature.xml")
    tree = ET.parse(feature_xml)
    root = tree.getroot()
    version = root.attrib.get("version", "1.0.0")  # Default version if not found
    version = version.strip()
    print("Version:", version)
    return version
    
# Build feature
def build_feature(capella_home, feature_project, version):
    print(f"[{feature_project}] Building feature for Capella Studio at: {capella_home} for [{version}]")
    command = [
       os.path.join(capella_home, "eclipse"),
        "-nosplash",
        "-application", "org.eclipse.ant.core.antRunner",
        "-buildfile", os.path.join("DEHCapellaAdapterFeature", "build.xml"),
        "-DbaseLocation=" + os.path.dirname("DEHCapellaAdapterFeature"),
        "-Ddestination=" + os.path.join("Release", feature_project, "bin"),
        "-DfeatureId=DEHCapellaAdapterFeature",
        "-DfeatureVersion=" + version,
        "-DarchiveFileName="+f"DEHCapellaAdapterFeature.{feature_project}.{version}.zip",
        "-DzipSuffix=true"
    ]
    
    # Run the command and capture output
    result = subprocess.run(command, capture_output=True, text=True)
    
    print("Feature build completed.")
    # Print output
    print("=== Feature Build Output ===")
    print(result.stdout)
    print("=== Feature Build Error ===")
    print(result.stderr)

# Generate readme
def generate_readme(version, date, hash_value):
    print("Generating README file...")
    readme_template = f"""\
# Capella DST Adapter

Product: Capella DST Adapter
Version: {version}
Date: {date}

SHA256: {hash_value}

Package content:
---------------
* DEH Capella feature with the DEH Capella Plugin

Installation:
------------
1) 	Unzip the content of the downloaded adapter plugin somewhere locally.
2)	In a running instance of Capella go to the “Help” menu and choose “Install new software”.
3)	From the “Install new software” dialog add a new source, choose local and browse to the location where you unzipped the adapter.
4)	The adapter should now be listed as available to install, if not try by unchecking the “Show items by category” checkbox.
5)	Select the adapter and press next to proceed to the installation.
6)	After the installation is complete Capella will invite you to restart Capella to complete the installation.

Initialize Hub server connection
-----------------------------
1) 	Opening of the adapter panels can be performed by pressing the button with the COMET icon in the tool bar. The button itself is a dropdown one, 
	where you can choose to open or close either the Impact View or the Hub Browser..
2) 	From the hub browser one click on connect.
3) 	Fill out the dialog boxes with your usual Comet credentials, once done the browsers.
	should show the ElementDefinition tree as well as the RequirementsSpecification tree.

SCENARIO instructions: Capella model to Hub
------------------------------------
1)	First, change the transfer direction so that the mapping source is the Capella model.
2)	Select one Element to be mapped with its children in one of the Capella element trees.
3)	Right click on your selection and select “Map Selection”.
4)	Map one of the selected Capella Element to one ElementDefinition or use the automatic mapping.
5)	Map one of the selected Requirement Element to one RequirementsSpecification or use the automatic mapping.
6)	Select the mapped element in the impact view that you wish to transfer.
7)	Click on Transfer
"""
    print(f"README generated for [{version}].")
    return readme_template


def update_abuild_xml(version):
    buildTemplate = os.path.join("DEHCapellaAdapterFeature", "build.xml.template")
    
    with open(buildTemplate, "r") as file:
        content = file.read()

    updated_content = content.replace("{version}", version)
    build = os.path.join("DEHCapellaAdapterFeature", "build.xml")
    
    with open(build, "w") as file:
        file.write(updated_content)

def main():
    # Define paths

    cap5_home = "D:\DomainSpecificTools\Capella\Other instal"
    cap6_home = "C:\Capella\CapellaStudio 6"
    feature_project = "DEHCapellaAdapterFeature"
    version = getVersion()
    update_abuild_xml(version)

    # Create directory for output
    output_dir = os.path.join("Releases")
    os.makedirs(output_dir, exist_ok=True)

    # Copy built features to output directory
    for feature in [("CapellaFeature5x", cap5_home), ("CapellaFeature6x", cap6_home)]:
        build_feature(feature[1], feature[0], version)
        print(f"Copying built feature {feature} to output directory...")
        shutil.copytree(
            os.path.join(feature_project, feature, "bin"),
            os.path.join(output_dir, feature)
        )
        print(f"Feature {feature} copied.")

    # Generate README files
    date = datetime.date.today().strftime("%Y-%m-%d")
    for feature in ["CapellaFeature5x", "CapellaFeature6x"]:
        print(f"[{feature}] Generating README file...")
        hash_value = subprocess.check_output(["sha256sum", os.path.join(output_dir, feature, "*.jar")])
        hash_value = hash_value.decode("utf-8").strip()
        readme_content = generate_readme(version, date, hash_value)
        with open(os.path.join(output_dir, feature, "README.md"), "w") as readme_file:
            readme_file.write(readme_content)
        print(f"[{feature}] README file  generated.")

    # Create zip files
    for feature in ["CapellaFeature5x", "CapellaFeature6x"]:
        print(f"[{feature}] Creating zip file...")
        shutil.make_archive(os.path.join(output_dir, feature), "zip", os.path.join(output_dir, feature))
        print(f"[{feature}] Zip file created.")

    print(f"[{version}] build completed. Zip files and READMEs are available at:", output_dir)

if __name__ == "__main__":
    main()
