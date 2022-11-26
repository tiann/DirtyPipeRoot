//
// Created by weishu on 2022/11/26.
//

#ifndef DIRTYPIPECHECK_EXPLOT_H
#define DIRTYPIPECHECK_EXPLOT_H

#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/user.h>

extern "C" {
    int exploit(const char *const path, loff_t offset, const char *const data);
}

#endif //DIRTYPIPECHECK_EXPLOT_H
