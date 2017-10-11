#!/bin/bash

[ -z "${BIOMEDICUS_XMX}" ] && BIOMEDICUS_XMX="2g"
export BIOMEDICUS_XMX

"$( dirname "${BASH_SOURCE[0]}" )/runClass.sh" edu.umn.biomedicus.uima.util.SimpleRunCPE $@
