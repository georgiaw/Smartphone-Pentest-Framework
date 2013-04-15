/* Routines required for instrumenting a program.  */
/* Compile this one with gcc.  */
/* Copyright (C) 1989, 1992, 1993, 1994, 1995, 1996, 1997, 1998, 1999,
   2000, 2001, 2002, 2003, 2004, 2005, 2008, 2009, 2010
   Free Software Foundation, Inc.

This file is part of GCC.

GCC is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free
Software Foundation; either version 3, or (at your option) any later
version.

GCC is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

Under Section 7 of GPL version 3, you are granted additional
permissions described in the GCC Runtime Library Exception, version
3.1, as published by the Free Software Foundation.

You should have received a copy of the GNU General Public License and
a copy of the GCC Runtime Library Exception along with this program;
see the files COPYING3 and COPYING.RUNTIME respectively.  If not, see
<http://www.gnu.org/licenses/>.  */

/* Assume compiling for Linux Kernel if __KERNEL__ is defined.  */
#ifdef __KERNEL__
 /* Define MACROs to be used by kernel compilation.  */
# define L_gcov
# define L_gcov_interval_profiler
# define L_gcov_pow2_profiler
# define L_gcov_one_value_profiler
# define L_gcov_indirect_call_profiler
# define L_gcov_average_profiler
# define L_gcov_ior_profiler

# define HAVE_CC_TLS 0
# define __GCOV_KERNEL__

# define IN_LIBGCOV 1
# define IN_GCOV 0
#else /* __KERNEL__ */
#include "tconfig.h"
#include "tsystem.h"
#include "coretypes.h"
#include "tm.h"
#endif /* __KERNEL__ */

#if 1
#define THREAD_PREFIX __thread
#else
#define THREAD_PREFIX
#endif

#ifndef __GCOV_KERNEL__
#if defined(inhibit_libc)
#define IN_LIBGCOV (-1)
#else
#undef NULL /* Avoid errors if stdio.h and our stddef.h mismatch.  */
#include <stdio.h>
#define IN_LIBGCOV 1
#if defined(L_gcov)
#define GCOV_LINKAGE /* nothing */
#endif
#endif
#endif /* __GCOV_KERNEL__ */

#include "gcov-io.h"

#if defined(inhibit_libc)
/* If libc and its header files are not available, provide dummy functions.  */

#ifdef L_gcov
void __gcov_init (struct gcov_info *p __attribute__ ((unused))) {}
void __gcov_flush (void) {}
#endif

#ifdef L_gcov_merge_add
void __gcov_merge_add (gcov_type *counters  __attribute__ ((unused)),
		       unsigned n_counters __attribute__ ((unused))) {}
#endif

#ifdef L_gcov_merge_single
void __gcov_merge_single (gcov_type *counters  __attribute__ ((unused)),
			  unsigned n_counters __attribute__ ((unused))) {}
#endif

#ifdef L_gcov_merge_delta
void __gcov_merge_delta (gcov_type *counters  __attribute__ ((unused)),
			 unsigned n_counters __attribute__ ((unused))) {}
#endif

#else

#ifndef __GCOV_KERNEL__
#include <string.h>
#if GCOV_LOCKED
#include <fcntl.h>
#include <errno.h>
#include <sys/stat.h>
#endif
#endif /* __GCOV_KERNEL__ */

#ifdef L_gcov
#include "gcov-io.c"

/* Utility function for outputing errors.  */
static int
gcov_error (const char *fmt, ...)
{
  int ret;
  va_list argp;
  va_start (argp, fmt);
#ifdef __GCOV_KERNEL__
  ret = vprintk (fmt, argp);
#else
  ret = vfprintf (stderr, fmt, argp);
#endif
  va_end (argp);
  return ret;
}

#ifndef __GCOV_KERNEL__
/* Emitted in coverage.c.  */
extern char * __gcov_pmu_profile_filename;
extern char * __gcov_pmu_profile_options;
extern gcov_unsigned_t __gcov_pmu_top_n_address;

/* Sampling rate.  */
extern gcov_unsigned_t __gcov_sampling_rate;
static int gcov_sampling_rate_initialized = 0;
void __gcov_set_sampling_rate (unsigned int rate);

/* Set sampling rate to RATE.  */

void __gcov_set_sampling_rate (unsigned int rate)
{
  __gcov_sampling_rate = rate;
}

/* Per thread sample counter.  */
THREAD_PREFIX gcov_unsigned_t __gcov_sample_counter = 0;

/* Chain of per-object gcov structures.  */
extern struct gcov_info *__gcov_list;

/* Size of the longest file name. */
static size_t gcov_max_filename = 0;
#endif /* __GCOV_KERNEL__ */

/* Unique identifier assigned to each module (object file).  */
static gcov_unsigned_t gcov_cur_module_id = 0;

/* Pointer to the direct-call counters (per call-site counters).
   Initialized by the caller.  */
THREAD_PREFIX gcov_type *__gcov_direct_call_counters ATTRIBUTE_HIDDEN;

/* Direct call callee address.  */
THREAD_PREFIX void *__gcov_direct_call_callee ATTRIBUTE_HIDDEN;

/* Pointer to the indirect-call counters (per call-site counters).
   Initialized by the caller.  */
THREAD_PREFIX gcov_type *__gcov_indirect_call_topn_counters ATTRIBUTE_HIDDEN;

/* Indirect call callee address.  */
THREAD_PREFIX void *__gcov_indirect_call_topn_callee ATTRIBUTE_HIDDEN;

/* A program checksum allows us to distinguish program data for an
   object file included in multiple programs.  */
static gcov_unsigned_t gcov_crc32;

/* Dynamic call graph build and form module groups.  */
void __gcov_compute_module_groups (void) ATTRIBUTE_HIDDEN;
void __gcov_finalize_dyn_callgraph (void) ATTRIBUTE_HIDDEN;

/* Profile summary for the gdca file, used in sanity check?  */
static struct gcov_summary all;

/* Profile summary for this program in current exeuction.  */
static struct gcov_summary this_program;

/* Profile summary for this object in current execuction.  */
static struct gcov_summary this_object;

/* Merged profile summary for this program.  */
static struct gcov_summary program;

/* Merged profile summary for this object.  */
static struct gcov_summary object;

/* Record the position of summary info.  */
static gcov_position_t summary_pos = 0;

/* Record the postion of eof.  */
static gcov_position_t eof_pos = 0;

/* Number of chars in prefix to be stripped.  */
static int gcov_prefix_strip = 0;

/* The length of path prefix.  */
static size_t prefix_length = 0;

/* gi_filename is current object filename.
   gi_filename_up points to the stripped filename.  */
static char *gi_filename, *gi_filename_up;

static int gcov_open_by_filename (char * gi_filename);
static int gcov_exit_init (void);
static void gcov_dump_one_gcov (struct gcov_info *gi_ptr);

/* Make sure path component of the given FILENAME exists, create
   missing directories. FILENAME must be writable.
   Returns zero on success, or -1 if an error occurred.  */

static int
create_file_directory (char *filename)
{
#if !defined(TARGET_POSIX_IO) && !defined(_WIN32)
  (void) filename;
  return -1;
#else
  char *s;

  s = filename;

  if (HAS_DRIVE_SPEC(s))
    s += 2;
  if (IS_DIR_SEPARATOR(*s))
    ++s;
  for (; *s != '\0'; s++)
    if (IS_DIR_SEPARATOR(*s))
      {
        char sep = *s;
	*s  = '\0';

        /* Try to make directory if it doesn't already exist.  */
        if (access (filename, F_OK) == -1
#ifdef TARGET_POSIX_IO
            && mkdir (filename, 0755) == -1
#else
            && mkdir (filename) == -1
#endif
            /* The directory might have been made by another process.  */
	    && errno != EEXIST)
	  {
            fprintf (stderr, "profiling:%s:Cannot create directory\n",
		     filename);
            *s = sep;
	    return -1;
	  };

	*s = sep;
      };
  return 0;
#endif
}

/* Open a file with the specified name.  */

static int
gcov_open_by_filename (char * gi_filename)
{
  if (!gcov_open (gi_filename))
    {
      /* Open failed likely due to missed directory.
         Create directory and retry to open file.  */
      if (create_file_directory (gi_filename))
        {
          gcov_error ("profiling:%s:Skip\n", gi_filename);
          return -1;
        }
      if (!gcov_open (gi_filename))
        {
          gcov_error ("profiling:%s:Cannot open\n", gi_filename);
          return -1;
        }
    }
  return 0;
}


/* Determine whether a counter is active.  */

static inline int
gcov_counter_active (const struct gcov_info *info, unsigned int type)
{
  return (1 << type) & info->ctr_mask;
}

#ifndef __GCOV_KERNEL__
/* Check if VERSION of the info block PTR matches libgcov one.
   Return 1 on success, or zero in case of versions mismatch.
   If FILENAME is not NULL, its value used for reporting purposes
   instead of value from the info block.  */

static int
gcov_version (struct gcov_info *ptr __attribute__ ((unused)), 
              gcov_unsigned_t version, const char *filename)
{
  if (version != GCOV_VERSION)
    {
      char v[4], e[4];

      GCOV_UNSIGNED2STRING (v, version);
      GCOV_UNSIGNED2STRING (e, GCOV_VERSION);

      if (filename)
        gcov_error ("profiling:%s:Version mismatch - expected %.4s got %.4s\n",
                   filename, e, v);
      else
        gcov_error ("profiling:Version mismatch - expected %.4s got %.4s\n", e, v);
      return 0;
    }
  return 1;
}

#define GCOV_GET_FILENAME gcov_strip_leading_dirs

/* Strip GCOV_PREFIX_STRIP levels of leading '/' from FILENAME and
   put the result into GI_FILENAME_UP.  */

static void
gcov_strip_leading_dirs (int prefix_length, int gcov_prefix_strip,
			 const char *filename, char *gi_filename_up)
{
  /* Avoid to add multiple drive letters into combined path.  */
  if (prefix_length != 0 && HAS_DRIVE_SPEC(filename))
    filename += 2;

  /* Build relocated filename, stripping off leading
     directories from the initial filename if requested. */
  if (gcov_prefix_strip > 0)
    {
      int level = 0;
      const char *s = filename;
      if (IS_DIR_SEPARATOR(*s))
	++s;

      /* Skip selected directory levels. */
      for (; (*s != '\0') && (level < gcov_prefix_strip); s++)
        if (IS_DIR_SEPARATOR(*s))
          {
            filename = s;
            level++;
          }
    }
  /* Update complete filename with stripped original. */
  if (prefix_length != 0 && !IS_DIR_SEPARATOR (*filename))
    {
      /* If prefix is given, add directory separator.  */
      strcpy (gi_filename_up, "/");
      strcpy (gi_filename_up + 1, filename);
    }
  else
    strcpy (gi_filename_up, filename);
}

/* This function allocates the space to store current file name.  */

static void
gcov_alloc_filename (void)
{
  /* Get file name relocation prefix.  Non-absolute values are ignored.  */
  char *gcov_prefix = 0;

  prefix_length = 0;
  gcov_prefix_strip = 0;

  {
    /* Check if the level of dirs to strip off specified. */
    char *tmp = getenv ("GCOV_PREFIX_STRIP");
    if (tmp)
      {
        gcov_prefix_strip = atoi (tmp);
        /* Do not consider negative values. */
        if (gcov_prefix_strip < 0)
          gcov_prefix_strip = 0;
      }
  }
  /* Get file name relocation prefix.  Non-absolute values are ignored. */
  gcov_prefix = getenv ("GCOV_PREFIX");
  if (gcov_prefix)
    {
      prefix_length = strlen(gcov_prefix);

      /* Remove an unnecessary trailing '/' */
      if (IS_DIR_SEPARATOR (gcov_prefix[prefix_length - 1]))
        prefix_length--;
    }
  else
    prefix_length = 0;

  /* If no prefix was specified and a prefix stip, then we assume
     relative.  */
  if (gcov_prefix_strip != 0 && prefix_length == 0)
    {
      gcov_prefix = ".";
      prefix_length = 1;
    }

  /* Allocate and initialize the filename scratch space.  */
  gi_filename = (char *) malloc (prefix_length + gcov_max_filename + 2);
  if (prefix_length)
    memcpy (gi_filename, gcov_prefix, prefix_length);

  gi_filename_up = gi_filename + prefix_length;
}

/* Stop the pmu profiler and dump pmu profile info into the global file.  */

static void
pmu_profile_stop (void)
{
  const char *pmu_profile_filename =  __gcov_pmu_profile_filename;
  const char *pmu_options = __gcov_pmu_profile_options;
  size_t filename_length;
  int gcda_error;

  if (!pmu_profile_filename || !pmu_options)
    return;

  __gcov_stop_pmu_profiler ();

  filename_length = strlen (pmu_profile_filename);
  if (filename_length > gcov_max_filename)
    gcov_max_filename = filename_length;
  /* Allocate and initialize the filename scratch space.  */
  gcov_alloc_filename ();
  GCOV_GET_FILENAME (prefix_length, gcov_prefix_strip, pmu_profile_filename,
                     gi_filename_up);
  /* Open the gcda file for writing. We don't support merge yet.  */
  gcda_error = gcov_open_by_filename (gi_filename);
  __gcov_end_pmu_profiler (gcda_error);
  if ((gcda_error = gcov_close ()))
    gcov_error (gcda_error  < 0 ?  "pmu_profile_stop:%s:Overflow writing\n" :
                "pmu_profile_stop:%s:Error writing\n",
                gi_filename);
}

/* Sort N entries in VALUE_ARRAY in descending order.
   Each entry in VALUE_ARRAY has two values. The sorting
   is based on the second value.  */

GCOV_LINKAGE  void
gcov_sort_n_vals (gcov_type *value_array, int n)
{
  int j, k;
  for (j = 2; j < n; j += 2)
    {
      gcov_type cur_ent[2];
      cur_ent[0] = value_array[j];
      cur_ent[1] = value_array[j + 1];
      k = j - 2;
      while (k >= 0 && value_array[k + 1] < cur_ent[1])
        {
          value_array[k + 2] = value_array[k];
          value_array[k + 3] = value_array[k+1];
          k -= 2;
        }
      value_array[k + 2] = cur_ent[0];
      value_array[k + 3] = cur_ent[1];
    }
}

/* Sort the profile counters for all indirect call sites. Counters
   for each call site are allocated in array COUNTERS.  */

static void
gcov_sort_icall_topn_counter (const struct gcov_ctr_info *counters)
{
  int i;
  gcov_type *values;
  int n = counters->num;
  gcc_assert (!(n % GCOV_ICALL_TOPN_NCOUNTS));

  values = counters->values;

  for (i = 0; i < n; i += GCOV_ICALL_TOPN_NCOUNTS)
    {
      gcov_type *value_array = &values[i + 1];
      gcov_sort_n_vals (value_array, GCOV_ICALL_TOPN_NCOUNTS - 1);
    }
}

/* Write imported files (auxiliary modules) for primary module GI_PTR
   into file GI_FILENAME.  */

static void
gcov_write_import_file (char *gi_filename, struct gcov_info *gi_ptr)
{
  char  *gi_imports_filename;
  const char *gcov_suffix;
  FILE *imports_file;
  size_t prefix_length, suffix_length;

  gcov_suffix = getenv ("GCOV_IMPORTS_SUFFIX");
  if (!gcov_suffix || !strlen (gcov_suffix))
    gcov_suffix = ".imports";
  suffix_length = strlen (gcov_suffix);
  prefix_length = strlen (gi_filename);
  gi_imports_filename = (char *) alloca (prefix_length + suffix_length + 1);
  memset (gi_imports_filename, 0, prefix_length + suffix_length + 1);
  memcpy (gi_imports_filename, gi_filename, prefix_length);
  memcpy (gi_imports_filename + prefix_length, gcov_suffix, suffix_length);
  imports_file = fopen (gi_imports_filename, "w");
  if (imports_file)
    {
      const struct dyn_imp_mod **imp_mods;
      unsigned i, imp_len;
      imp_mods = gcov_get_sorted_import_module_array (gi_ptr, &imp_len);
      if (imp_mods)
        {
          for (i = 0; i < imp_len; i++)
	    {
	      fprintf (imports_file, "%s\n",
		       imp_mods[i]->imp_mod->mod_info->source_filename);
	      fprintf (imports_file, "%s%s\n",
		       imp_mods[i]->imp_mod->mod_info->da_filename, GCOV_DATA_SUFFIX);
	    }
          free (imp_mods);
        }
      fclose (imports_file);
    }
}

static void
gcov_dump_module_info (void)
{
  struct gcov_info *gi_ptr;

  __gcov_compute_module_groups ();

  /* Now write out module group info.  */
  for (gi_ptr = __gcov_list; gi_ptr; gi_ptr = gi_ptr->next)
  {
    int error;

    GCOV_GET_FILENAME (prefix_length, gcov_prefix_strip, gi_ptr->filename,
                       gi_filename_up);
    error = gcov_open_by_filename (gi_filename);
    if (error != 0)
      continue;

    /* Overwrite the zero word at the of the file.  */
    gcov_rewrite ();
    gcov_seek (gi_ptr->eof_pos);

    gcov_write_module_infos (gi_ptr);
    gcov_truncate ();

    if ((error = gcov_close ()))
         gcov_error (error  < 0 ?  "profiling:%s:Overflow writing\n" :
                                   "profiling:%s:Error writing\n",
                                   gi_filename);
    gcov_write_import_file (gi_filename, gi_ptr);
  }
  __gcov_finalize_dyn_callgraph ();
}

/* Dump the coverage counts. We merge with existing counts when
   possible, to avoid growing the .da files ad infinitum. We use this
   program's checksum to make sure we only accumulate whole program
   statistics to the correct summary. An object file might be embedded
   in two separate programs, and we must keep the two program
   summaries separate.  */

static void
gcov_exit (void)
{
  struct gcov_info *gi_ptr;
  int dump_module_info;

  /* Stop and write the PMU profile data into the global file.  */
  pmu_profile_stop ();

  dump_module_info = gcov_exit_init ();

  for (gi_ptr = __gcov_list; gi_ptr; gi_ptr = gi_ptr->next)
    gcov_dump_one_gcov (gi_ptr);

  if (dump_module_info)
    gcov_dump_module_info ();

  free (gi_filename);
}

/* Add a new object file onto the bb chain.  Invoked automatically
   when running an object file's global ctors.  */

void
__gcov_init (struct gcov_info *info)
{
  if (!gcov_sampling_rate_initialized)
    {
      const char* env_value_str = getenv ("GCOV_SAMPLING_RATE");
      if (env_value_str)
        {
          int env_value_int = atoi(env_value_str);
          if (env_value_int >= 1)
            __gcov_sampling_rate = env_value_int;
        }
      gcov_sampling_rate_initialized = 1;
    }

  if (!info->version)
    return;

  if (gcov_version (info, info->version, 0))
    {
      const char *ptr = info->filename;
      gcov_unsigned_t crc32 = gcov_crc32;
      size_t filename_length = strlen (info->filename);
      struct gcov_pmu_info pmu_info;

      /* Refresh the longest file name information.  */
      if (filename_length > gcov_max_filename)
        gcov_max_filename = filename_length;

      /* Initialize the pmu profiler.  */
      pmu_info.pmu_profile_filename = __gcov_pmu_profile_filename;
      pmu_info.pmu_tool = __gcov_pmu_profile_options;
      pmu_info.pmu_top_n_address = __gcov_pmu_top_n_address;
      __gcov_init_pmu_profiler (&pmu_info);
      if (pmu_info.pmu_profile_filename)
        {
          /* Refresh the longest file name information.  */
          filename_length = strlen (pmu_info.pmu_profile_filename);
          if (filename_length > gcov_max_filename)
            gcov_max_filename = filename_length;
        }

      /* Assign the module ID (starting at 1).  */
      info->mod_info->ident = (++gcov_cur_module_id);
      gcc_assert (EXTRACT_MODULE_ID_FROM_GLOBAL_ID (GEN_FUNC_GLOBAL_ID (
                                                       info->mod_info->ident, 0))
                  == info->mod_info->ident);

      do
	{
	  unsigned ix;
	  gcov_unsigned_t value = *ptr << 24;

	  for (ix = 8; ix--; value <<= 1)
	    {
	      gcov_unsigned_t feedback;

	      feedback = (value ^ crc32) & 0x80000000 ? 0x04c11db7 : 0;
	      crc32 <<= 1;
	      crc32 ^= feedback;
	    }
	} while (*ptr++);

      gcov_crc32 = crc32;

      if (!__gcov_list)
        {
          atexit (gcov_exit);
          /* Start pmu profiler. */
          __gcov_start_pmu_profiler ();
        }

      info->next = __gcov_list;
      __gcov_list = info;
    }
  info->version = 0;
}

/* Called before fork or exec - write out profile information gathered so
   far and reset it to zero.  This avoids duplication or loss of the
   profile information gathered so far.  */

void
__gcov_flush (void)
{
  const struct gcov_info *gi_ptr;

  __gcov_stop_pmu_profiler ();
  gcov_exit ();
  for (gi_ptr = __gcov_list; gi_ptr; gi_ptr = gi_ptr->next)
    {
      unsigned t_ix;
      const struct gcov_ctr_info *ci_ptr;

      for (t_ix = 0, ci_ptr = gi_ptr->counts; t_ix != GCOV_COUNTERS; t_ix++)
        if (gcov_counter_active (gi_ptr, t_ix))
	  {
	    memset (ci_ptr->values, 0, sizeof (gcov_type) * ci_ptr->num);
	    ci_ptr++;
	  }
    }
  __gcov_start_pmu_profiler ();
}

#else /* __GCOV_KERNEL__ */

#define GCOV_GET_FILENAME gcov_get_filename

/* Copy the filename to the buffer.  */

static inline void
gcov_get_filename (int prefix_length __attribute__ ((unused)),
                   int gcov_prefix_strip __attribute__ ((unused)),
                   const char *filename, char *gi_filename_up)
{
    strcpy (gi_filename_up, filename);
}

/* Sort the profile counters for all indirect call sites. Counters
   for each call site are allocated in array COUNTERS.  */

static void
gcov_sort_icall_topn_counter (const struct gcov_ctr_info *counters)
{
  /* Empty */
}

/* Reserves a buffer to store the name of the file being processed.  */
static char _kernel_gi_filename[520];

/* This function allocates the space to store current file name.  */

static void
gcov_alloc_filename (void)
{
  prefix_length = 0;
  gcov_prefix_strip = 0;
  gi_filename = _kernel_gi_filename;
  gi_filename_up = _kernel_gi_filename;
}

#endif /* __GCOV_KERNEL__ */

/* Determine number of active counters in gcov_info INFO,
   the counter arrays are stored in VALUES if the coming
   value of VALUES !=0. If FLAG_SORT_ICALL_TOPN_COUNTER !=0,
   the icall_topn_counter in INFO will be sorted.
   Return: the number of active counter types.  */

static unsigned int
gcov_counter_array (const struct gcov_info *info,
                    gcov_type *values[GCOV_COUNTERS],
                    int flag_sort_icall_topn_counter)
{
  unsigned int i;
  unsigned int result = 0;

  for (i = 0; i < GCOV_COUNTERS; i++) {
    if (gcov_counter_active (info, i))
      {
        if (values)
          values[result] = info->counts[result].values;
        if (flag_sort_icall_topn_counter &&
            (i == GCOV_COUNTER_ICALL_TOPNV))
          gcov_sort_icall_topn_counter (&info->counts[result]);
        result++;
      }
  }
  return result;
}

/* Compute object summary recored in gcov_info INFO. The result is
   stored in OBJ_SUM. Note that the caller is responsible for
   zeroing out OBJ_SUM, otherwise the summary is accumulated.  */

static void
gcov_object_summary (struct gcov_info *info,
                     struct gcov_summary *obj_sum)
{
  const struct gcov_ctr_info *ci_ptr;
  struct gcov_ctr_summary *cs_ptr;
  gcov_unsigned_t c_num;
  unsigned t_ix;

  /* Totals for this object file.  */
  ci_ptr = info->counts;
  for (t_ix = 0; t_ix < GCOV_COUNTERS_SUMMABLE; t_ix++)
    {
      if (!gcov_counter_active (info, t_ix))
        continue;

      cs_ptr = &(obj_sum->ctrs[t_ix]);
      cs_ptr->num += ci_ptr->num;
      for (c_num = 0; c_num < ci_ptr->num; c_num++)
        {
          cs_ptr->sum_all += ci_ptr->values[c_num];
          if (cs_ptr->run_max < ci_ptr->values[c_num])
            cs_ptr->run_max = ci_ptr->values[c_num];
        }
      ci_ptr++;
    }
}

/* Merge with existing gcda file in the same directory to avoid
   excessive growthe of the files.  */

static int
gcov_merge_gcda_file (struct gcov_info *info,
                      gcov_type *values[GCOV_COUNTERS],
                      unsigned fi_stride)
{
  struct gcov_ctr_summary *cs_obj, *cs_tobj, *cs_prg, *cs_tprg, *cs_all;
  unsigned t_ix, f_ix;

#ifndef __GCOV_KERNEL__
  const struct gcov_fn_info *fi_ptr;
  unsigned c_ix, n_counts;
  int error = 0;
  gcov_unsigned_t tag, length;

  eof_pos = 0;
  summary_pos = 0;

  tag = gcov_read_unsigned ();
  if (tag)
    {
      /* Merge data from file.  */
      if (tag != GCOV_DATA_MAGIC)
        {
          gcov_error ("profiling:%s:Not a gcov data file\n", gi_filename);
          goto read_fatal;
        }
     length = gcov_read_unsigned ();
     if (!gcov_version (info, length, gi_filename))
       goto read_fatal;

     length = gcov_read_unsigned ();
     if (length != info->stamp)
       /* Read from a different compilation. Overwrite the file.  */
       goto rewrite;

     /* Merge execution counts for each function.  */
     for (f_ix = 0; f_ix < info->n_functions; f_ix++)
       {
         fi_ptr = (const struct gcov_fn_info *)
                   ((const char *) info->functions + f_ix * fi_stride);
         tag = gcov_read_unsigned ();
         length = gcov_read_unsigned ();

         /* Check function.  */
         if (tag != GCOV_TAG_FUNCTION
	     || length != GCOV_TAG_FUNCTION_LENGTH
             || gcov_read_unsigned () != fi_ptr->ident
             || gcov_read_unsigned () != fi_ptr->lineno_checksum
             || gcov_read_unsigned () != fi_ptr->cfg_checksum)
           goto read_mismatch;

           c_ix = 0;
           for (t_ix = 0; t_ix < GCOV_COUNTERS; t_ix++)
             {
               gcov_merge_fn merge;

               if (!((1 << t_ix) & info->ctr_mask))
                 continue;

               n_counts = fi_ptr->n_ctrs[c_ix];
               merge = info->counts[c_ix].merge;

               tag = gcov_read_unsigned ();
               length = gcov_read_unsigned ();
               if (tag != GCOV_TAG_FOR_COUNTER (t_ix)
                   || length != GCOV_TAG_COUNTER_LENGTH (n_counts))
                 goto read_mismatch;
               (*merge) (values[c_ix], n_counts);
               values[c_ix] += n_counts;
               c_ix++;
             }
           if ((error = gcov_is_error ()))
             goto read_error;
       }

       f_ix = ~0u;
       /* Check program & object summary.  */
       while (1)
         {
           int is_program;

           eof_pos = gcov_position ();
           tag = gcov_read_unsigned ();
           if (!tag)
             break;

           length = gcov_read_unsigned ();
           is_program = tag == GCOV_TAG_PROGRAM_SUMMARY;
           if (length != GCOV_TAG_SUMMARY_LENGTH
               || (!is_program && tag != GCOV_TAG_OBJECT_SUMMARY))
             goto read_mismatch;
           gcov_read_summary (is_program ? &program : &object);
           if ((error = gcov_is_error ()))
             goto read_error;
           if (is_program && program.checksum == gcov_crc32)
             {
               summary_pos = eof_pos;
               goto rewrite;
             }
         }
    }

    goto rewrite;

read_error:;
    gcov_error (error < 0 ? "profiling:%s:Overflow merging\n"
                : "profiling:%s:Error merging\n", gi_filename);
    goto read_fatal;

#endif /* __GCOV_KERNEL__ */

    goto rewrite;

read_mismatch:;
    gcov_error ("profiling:%s:Merge mismatch for %s\n", gi_filename,
                 f_ix + 1 ? "function" : "summaries");
    goto read_fatal; /* work-around the compiler warning */

read_fatal:;
    gcov_close ();
    return 1;

rewrite:;
    gcov_rewrite ();
    if (!summary_pos)
      memset (&program, 0, sizeof (program));

    /* Merge the summaries.  */
    f_ix = ~0u;
    for (t_ix = 0; t_ix < GCOV_COUNTERS_SUMMABLE; t_ix++)
      {
        cs_obj = &object.ctrs[t_ix];
        cs_tobj = &this_object.ctrs[t_ix];
        cs_prg = &program.ctrs[t_ix];
        cs_tprg = &this_program.ctrs[t_ix];
        cs_all = &all.ctrs[t_ix];

        if ((1 << t_ix) & info->ctr_mask)
          {
            if (!cs_obj->runs++)
              cs_obj->num = cs_tobj->num;
            else if (cs_obj->num != cs_tobj->num)
               goto read_mismatch;
            cs_obj->sum_all += cs_tobj->sum_all;
            if (cs_obj->run_max < cs_tobj->run_max)
              cs_obj->run_max = cs_tobj->run_max;
            cs_obj->sum_max += cs_tobj->run_max;

            if (!cs_prg->runs++)
              cs_prg->num = cs_tprg->num;
            else if (cs_prg->num != cs_tprg->num)
              goto read_mismatch;
            cs_prg->sum_all += cs_tprg->sum_all;
            if (cs_prg->run_max < cs_tprg->run_max)
              cs_prg->run_max = cs_tprg->run_max;
            cs_prg->sum_max += cs_tprg->run_max;
          }
        else if (cs_obj->num || cs_prg->num)
          goto read_mismatch;

        if (!cs_all->runs && cs_prg->runs)
          memcpy (cs_all, cs_prg, sizeof (*cs_all));
        else if (!all.checksum
                 && (!GCOV_LOCKED || cs_all->runs == cs_prg->runs)
                 && memcmp (cs_all, cs_prg, sizeof (*cs_all)))
          {
            gcov_error ("profiling:%s:Invocation mismatch - "
                "some data files may have been removed%s",
            gi_filename, GCOV_LOCKED
            ? "" : " or concurrent update without locking support");
            all.checksum = ~0u;
          }
      }

  return 0;
}

/* Calculate the function_info stride. This depends on the
   number of counter types being measured.
   NUM_COUNTER_TYPES is number of counter types recorded.
   Return: the number of bytes for accessing next fn_info
   (aligned to gcov_fn_info).  */

static unsigned
gcov_compute_fi_stride (unsigned num_counter_types)
{
   unsigned fi_stride;

   fi_stride = offsetof (struct gcov_fn_info, n_ctrs) +
               num_counter_types * sizeof (unsigned);
   if (__alignof__ (struct gcov_fn_info) > sizeof (unsigned))
   {
     fi_stride += __alignof__ (struct gcov_fn_info) - 1;
     fi_stride &= ~(__alignof__ (struct gcov_fn_info) - 1);
   }
   return fi_stride;
}

/* This function returns the size of gcda file to be written. Note
   the size is in units of gcov_type.  */

GCOV_LINKAGE unsigned
gcov_gcda_file_size (struct gcov_info *gi_ptr)
{
  unsigned size;
  const struct gcov_fn_info *fi_ptr;
  unsigned f_ix, t_ix, c_ix;
  unsigned n_counts;
  unsigned fi_stride;
  gcov_type *values[GCOV_COUNTERS];

  c_ix = gcov_counter_array (gi_ptr, values, 0);
  fi_stride = gcov_compute_fi_stride (c_ix);

  /* GCOV_DATA_MAGIC, GCOV_VERSION and time_stamp.  */
  size = 3;

  /* size for each function.  */
  for (f_ix = 0; f_ix < gi_ptr->n_functions; f_ix++)
    {
      fi_ptr = (const struct gcov_fn_info *)
        ((const char *) gi_ptr->functions + f_ix * fi_stride);

      size += 2 /* tag_length itself */
              + GCOV_TAG_FUNCTION_LENGTH; /* ident, lineno_cksum, cfg_cksm */

      c_ix = 0;
      for (t_ix = 0; t_ix < GCOV_COUNTERS; t_ix++)
        {
          if (!((1 << t_ix) & gi_ptr->ctr_mask))
            continue;

          n_counts = fi_ptr->n_ctrs[c_ix];
          size += 2 + GCOV_TAG_COUNTER_LENGTH (n_counts);
          c_ix++;
        }
    }

  /* Object summary.  */
  size += 2 + GCOV_TAG_SUMMARY_LENGTH;

  /* Program summary.  */
  size += 2 + GCOV_TAG_SUMMARY_LENGTH;

  size += 1;

  return size*4;
}

/* Write profile data (including summary and module grouping information,
   if available, to file.  */

static void
gcov_write_gcda_file (struct gcov_info *gi_ptr,
                      unsigned fi_stride)
{
      const struct gcov_fn_info *fi_ptr;
      gcov_type *values[GCOV_COUNTERS];
      unsigned t_ix, c_ix, f_ix, n_counts;
      int error = 0;

      /* Write out the data.  */
      gcov_write_tag_length (GCOV_DATA_MAGIC, GCOV_VERSION);
      gcov_write_unsigned (gi_ptr->stamp);

      gcov_counter_array (gi_ptr, values, 0);

      /* Write execution counts for each function.  */
      for (f_ix = 0; f_ix < gi_ptr->n_functions; f_ix++)
        {
	  fi_ptr = (const struct gcov_fn_info *)
		  ((const char *) gi_ptr->functions + f_ix * fi_stride);

	  /* Announce function.  */
	  gcov_write_tag_length (GCOV_TAG_FUNCTION, GCOV_TAG_FUNCTION_LENGTH);
	  gcov_write_unsigned (fi_ptr->ident);
	  gcov_write_unsigned (fi_ptr->lineno_checksum);
	  gcov_write_unsigned (fi_ptr->cfg_checksum);

	  c_ix = 0;
	  for (t_ix = 0; t_ix < GCOV_COUNTERS; t_ix++)
	    {
	      gcov_type *c_ptr;

	      if (!((1 << t_ix) & gi_ptr->ctr_mask))
		continue;

	      n_counts = fi_ptr->n_ctrs[c_ix];

	      gcov_write_tag_length (GCOV_TAG_FOR_COUNTER (t_ix),
				     GCOV_TAG_COUNTER_LENGTH (n_counts));
	      c_ptr = values[c_ix];
	      while (n_counts--)
		gcov_write_counter (*c_ptr++);

	      values[c_ix] = c_ptr;
	      c_ix++;
	    }
        }

      /* Object file summary.  */
      gcov_write_summary (GCOV_TAG_OBJECT_SUMMARY, &object);

      /* Generate whole program statistics.  */
      program.checksum = gcov_crc32;
      if (eof_pos)
	gcov_seek (eof_pos);
      gcov_write_summary (GCOV_TAG_PROGRAM_SUMMARY, &program);
      if (!summary_pos)
	gcov_write_unsigned (0);

      /* TODO: there is a problem here -- if there are other program
         summary data after the matching one, setting eof_pos to this
         position means that the module info table will overwrite the
         those other program summary. It also means a mismatch error
         may occur at the next merge if no matching program summary is
         found before the module info data.  */
      if (!summary_pos)
        gi_ptr->eof_pos = gcov_position () - 1;
      else
        gi_ptr->eof_pos = gcov_position ();

      if ((error = gcov_close ()))
	  gcov_error (error  < 0 ?
		   "profiling:%s:Overflow writing\n" :
		   "profiling:%s:Error writing\n",
		   gi_filename);
}

/* Do some preparation work before calling the actual dumping
   routine.
   Return: 1 when module grouping info needs to be dumped,
           0 otherwise.  */

static int
gcov_exit_init (void)
{
  struct gcov_info *gi_ptr;
  int dump_module_info = 0;

  dump_module_info = 0;
  gcov_prefix_strip = 0;

  memset (&all, 0, sizeof (all));

  /* Find the totals for this execution.  */
  memset (&this_program, 0, sizeof (this_program));
  for (gi_ptr = __gcov_list; gi_ptr; gi_ptr = gi_ptr->next)
    {
      gcov_object_summary (gi_ptr, &this_program);

      /* The IS_PRIMARY field is overloaded to indicate if this module
         is FDO/LIPO.  */
      dump_module_info |= gi_ptr->mod_info->is_primary;
    }

  gcov_alloc_filename ();

  return dump_module_info;
}

/* Dump one entry in the gcov_info list (for one object).  */

static void
gcov_dump_one_gcov (struct gcov_info *gi_ptr)
{
  gcov_type *values[GCOV_COUNTERS];
  unsigned fi_stride;
  unsigned c_ix;
  int ret;

  memset (&this_object, 0, sizeof (this_object));
  memset (&object, 0, sizeof (object));

  gcov_object_summary (gi_ptr, &this_object);

  c_ix = gcov_counter_array (gi_ptr, values, 1);

  fi_stride = gcov_compute_fi_stride (c_ix);

  GCOV_GET_FILENAME (prefix_length, gcov_prefix_strip, gi_ptr->filename,
                     gi_filename_up);

  if (gcov_open_by_filename (gi_filename) == -1)
    return;

  /* Now merge this file.  */
  ret = gcov_merge_gcda_file (gi_ptr, values, fi_stride);
  if (ret != 0 ) return;

  gcov_write_gcda_file (gi_ptr, fi_stride);
}

#endif /* L_gcov */

#ifdef L_gcov_merge_add
/* The profile merging function that just adds the counters.  It is given
   an array COUNTERS of N_COUNTERS old counters and it reads the same number
   of counters from the gcov file.  */
void
__gcov_merge_add (gcov_type *counters, unsigned n_counters)
{
  for (; n_counters; counters++, n_counters--)
    *counters += gcov_read_counter ();
}
#endif /* L_gcov_merge_add */

#ifdef L_gcov_merge_ior
/* The profile merging function that just adds the counters.  It is given
   an array COUNTERS of N_COUNTERS old counters and it reads the same number
   of counters from the gcov file.  */
void
__gcov_merge_ior (gcov_type *counters, unsigned n_counters)
{
  for (; n_counters; counters++, n_counters--)
    *counters |= gcov_read_counter ();
}
#endif

#ifdef L_gcov_merge_reusedist

/* Return the weighted arithmetic mean of two values.  */

static gcov_type
__gcov_weighted_mean2 (gcov_type value1, gcov_type count1,
                       gcov_type value2, gcov_type count2)
{
  if (count1 + count2 == 0)
    return 0;
  else
    return (value1 * count1 + value2 * count2) / (count1 + count2);
}

void
__gcov_merge_reusedist (gcov_type *counters, unsigned n_counters)
{
  unsigned i;

  gcc_assert(!(n_counters % 4));

  for (i = 0; i < n_counters; i += 4)
    {
      /* Decode current values.  */
      gcov_type c_mean_dist = counters[i];
      gcov_type c_mean_size = counters[i+1];
      gcov_type c_count = counters[i+2];
      gcov_type c_dist_x_size = counters[i+3];

      /* Read and decode values in file.  */
      gcov_type f_mean_dist = __gcov_read_counter ();
      gcov_type f_mean_size = __gcov_read_counter ();
      gcov_type f_count = __gcov_read_counter ();
      gcov_type f_dist_x_size = __gcov_read_counter ();

      /* Compute aggregates.  */
      gcov_type a_mean_dist = __gcov_weighted_mean2 (
          f_mean_dist, f_count, c_mean_dist, c_count);
      gcov_type a_mean_size = __gcov_weighted_mean2 (
          f_mean_size, f_count, c_mean_size, c_count);
      gcov_type a_count = f_count + c_count;
      gcov_type a_dist_x_size = f_dist_x_size + c_dist_x_size;

      /* Encode back into counters.  */
      counters[i] = a_mean_dist;
      counters[i+1] = a_mean_size;
      counters[i+2] = a_count;
      counters[i+3] = a_dist_x_size;
    }
}

#endif

#ifdef L_gcov_merge_dc

/* Returns 1 if the function global id GID is not valid.  */

static int
__gcov_is_gid_insane (gcov_type gid)
{
  if (EXTRACT_MODULE_ID_FROM_GLOBAL_ID (gid) == 0
      || EXTRACT_FUNC_ID_FROM_GLOBAL_ID (gid) == 0)
    return 1;
  return 0;
}

/* The profile merging function used for merging direct call counts
   This function is given array COUNTERS of N_COUNTERS old counters and it
   reads the same number of counters from the gcov file.  */

void
__gcov_merge_dc (gcov_type *counters, unsigned n_counters)
{
  unsigned i;

  gcc_assert (!(n_counters % 2));
  for (i = 0; i < n_counters; i += 2)
    {
      gcov_type global_id = gcov_read_counter ();
      gcov_type call_count = gcov_read_counter ();

      /* Note that global id counter may never have been set if no calls were
	 made from this call-site.  */
      if (counters[i] && global_id)
        {
          /* TODO race condition requires us do the following correction.  */
          if (__gcov_is_gid_insane (counters[i]))
            counters[i] = global_id;
          else if (__gcov_is_gid_insane (global_id))
            global_id = counters[i];

          gcc_assert (counters[i] == global_id);
        }
      else if (global_id)
	counters[i] = global_id;

      counters[i + 1] += call_count;

      /* Reset. */
      if (__gcov_is_gid_insane (counters[i]))
        counters[i] = counters[i + 1] = 0;

      /* Assert that the invariant (global_id == 0) <==> (call_count == 0)
	 holds true after merging.  */
      if (counters[i] == 0)
        counters[i+1] = 0;
      if (counters[i + 1] == 0)
        counters[i] = 0;
    }
}
#endif

#ifdef L_gcov_merge_icall_topn
/* The profile merging function used for merging indirect call counts
   This function is given array COUNTERS of N_COUNTERS old counters and it
   reads the same number of counters from the gcov file.  */

void
__gcov_merge_icall_topn (gcov_type *counters, unsigned n_counters)
{
  unsigned i, j, k, m;

  gcc_assert (!(n_counters % GCOV_ICALL_TOPN_NCOUNTS));
  for (i = 0; i < n_counters; i += GCOV_ICALL_TOPN_NCOUNTS)
    {
      gcov_type *value_array = &counters[i + 1];
      unsigned tmp_size = 2 * (GCOV_ICALL_TOPN_NCOUNTS - 1);
      gcov_type *tmp_array 
          = (gcov_type *) alloca (tmp_size * sizeof (gcov_type));

      for (j = 0; j < tmp_size; j++)
        tmp_array[j] = 0;

      for (j = 0; j < GCOV_ICALL_TOPN_NCOUNTS - 1; j += 2)
        {
          tmp_array[j] = value_array[j];
          tmp_array[j + 1] = value_array [j + 1];
        }

      /* Skip the number_of_eviction entry.  */
      gcov_read_counter ();
      for (k = 0; k < GCOV_ICALL_TOPN_NCOUNTS - 1; k += 2)
        {
          int found = 0;
          gcov_type global_id = gcov_read_counter ();
          gcov_type call_count = gcov_read_counter ();
          for (m = 0; m < j; m += 2)
            {
              if (tmp_array[m] == global_id)
                {
                  found = 1;
                  tmp_array[m + 1] += call_count;
                  break;
                }
            }
          if (!found)
            {
              tmp_array[j] = global_id;
              tmp_array[j + 1] = call_count;
              j += 2;
            }
        }
      /* Now sort the temp array */
      gcov_sort_n_vals (tmp_array, j);

      /* Now copy back the top half of the temp array */
      for (k = 0; k < GCOV_ICALL_TOPN_NCOUNTS - 1; k += 2)
        {
          value_array[k] = tmp_array[k];
          value_array[k + 1] = tmp_array[k + 1];
        }
    }
}
#endif


#ifdef L_gcov_merge_single
/* The profile merging function for choosing the most common value.
   It is given an array COUNTERS of N_COUNTERS old counters and it
   reads the same number of counters from the gcov file.  The counters
   are split into 3-tuples where the members of the tuple have
   meanings:

   -- the stored candidate on the most common value of the measured entity
   -- counter
   -- total number of evaluations of the value  */
void
__gcov_merge_single (gcov_type *counters, unsigned n_counters)
{
  unsigned i, n_measures;
  gcov_type value, counter, all;

  gcc_assert (!(n_counters % 3));
  n_measures = n_counters / 3;
  for (i = 0; i < n_measures; i++, counters += 3)
    {
      value = gcov_read_counter ();
      counter = gcov_read_counter ();
      all = gcov_read_counter ();

      if (counters[0] == value)
	counters[1] += counter;
      else if (counter > counters[1])
	{
	  counters[0] = value;
	  counters[1] = counter - counters[1];
	}
      else
	counters[1] -= counter;
      counters[2] += all;
    }
}
#endif /* L_gcov_merge_single */

#ifdef L_gcov_merge_delta
/* The profile merging function for choosing the most common
   difference between two consecutive evaluations of the value.  It is
   given an array COUNTERS of N_COUNTERS old counters and it reads the
   same number of counters from the gcov file.  The counters are split
   into 4-tuples where the members of the tuple have meanings:

   -- the last value of the measured entity
   -- the stored candidate on the most common difference
   -- counter
   -- total number of evaluations of the value  */
void
__gcov_merge_delta (gcov_type *counters, unsigned n_counters)
{
  unsigned i, n_measures;
  gcov_type value, counter, all;

  gcc_assert (!(n_counters % 4));
  n_measures = n_counters / 4;
  for (i = 0; i < n_measures; i++, counters += 4)
    {
      /* last = */ gcov_read_counter ();
      value = gcov_read_counter ();
      counter = gcov_read_counter ();
      all = gcov_read_counter ();

      if (counters[1] == value)
	counters[2] += counter;
      else if (counter > counters[2])
	{
	  counters[1] = value;
	  counters[2] = counter - counters[2];
	}
      else
	counters[2] -= counter;
      counters[3] += all;
    }
}
#endif /* L_gcov_merge_delta */

#ifdef L_gcov_interval_profiler
/* If VALUE is in interval <START, START + STEPS - 1>, then increases the
   corresponding counter in COUNTERS.  If the VALUE is above or below
   the interval, COUNTERS[STEPS] or COUNTERS[STEPS + 1] is increased
   instead.  */

void
__gcov_interval_profiler (gcov_type *counters, gcov_type value,
			  int start, unsigned steps)
{
  gcov_type delta = value - start;
  if (delta < 0)
    counters[steps + 1]++;
  else if (delta >= steps)
    counters[steps]++;
  else
    counters[delta]++;
}
#endif

#ifdef L_gcov_pow2_profiler
/* If VALUE is a power of two, COUNTERS[1] is incremented.  Otherwise
   COUNTERS[0] is incremented.  */

void
__gcov_pow2_profiler (gcov_type *counters, gcov_type value)
{
  if (value & (value - 1))
    counters[0]++;
  else
    counters[1]++;
}
#endif

/* Tries to determine the most common value among its inputs.  Checks if the
   value stored in COUNTERS[0] matches VALUE.  If this is the case, COUNTERS[1]
   is incremented.  If this is not the case and COUNTERS[1] is not zero,
   COUNTERS[1] is decremented.  Otherwise COUNTERS[1] is set to one and
   VALUE is stored to COUNTERS[0].  This algorithm guarantees that if this
   function is called more than 50% of the time with one value, this value
   will be in COUNTERS[0] in the end.

   In any case, COUNTERS[2] is incremented.  */

static inline void
__gcov_one_value_profiler_body (gcov_type *counters, gcov_type value)
{
  if (value == counters[0])
    counters[1]++;
  else if (counters[1] == 0)
    {
      counters[1] = 1;
      counters[0] = value;
    }
  else
    counters[1]--;
  counters[2]++;
}

#ifdef L_gcov_indirect_call_topn_profiler
/* Tries to keep track the most frequent N values in the counters where
   N is specified by parameter TOPN_VAL. To track top N values, 2*N counter
   entries are used.
   counter[0] --- the accumative count of the number of times one entry in
                  in the counters gets evicted/replaced due to limited capacity.
                  When this value reaches a threshold, the bottom N values are
                  cleared.
   counter[1] through counter[2*N] records the top 2*N values collected so far.
   Each value is represented by two entries: count[2*i+1] is the ith value, and
   count[2*i+2] is the number of times the value is seen.  */

static void
__gcov_topn_value_profiler_body (gcov_type *counters, gcov_type value,
                                 gcov_unsigned_t topn_val)
{
   unsigned i, found = 0, have_zero_count = 0;

   gcov_type *entry;
   gcov_type *lfu_entry = &counters[1];
   gcov_type *value_array = &counters[1];
   gcov_type *num_eviction = &counters[0];

   /* There are 2*topn_val values tracked, each value takes two slots in the
      counter array */
   for ( i = 0; i < (topn_val << 2); i += 2)
     {
       entry = &value_array[i];
       if ( entry[0] == value)
         {
           entry[1]++ ;
           found = 1;
           break;
         }
       else if (entry[1] == 0)
         {
           lfu_entry = entry;
           have_zero_count = 1;
         }
      else if (entry[1] < lfu_entry[1])
        lfu_entry = entry;
     }

   if (found)
     return;

   /* lfu_entry is either an empty entry or an entry
      with lowest count, which will be evicted.  */
   lfu_entry[0] = value;
   lfu_entry[1] = 1;

#define GCOV_ICALL_COUNTER_CLEAR_THRESHOLD 3000

   /* Too many evictions -- time to clear bottom entries to 
      avoid hot values bumping each other out.  */
   if ( !have_zero_count 
        && ++*num_eviction >= GCOV_ICALL_COUNTER_CLEAR_THRESHOLD)
     {
       unsigned i, j;
       gcov_type *p, minv;
       gcov_type* tmp_cnts 
           = (gcov_type *)alloca (topn_val * sizeof(gcov_type));

       *num_eviction = 0;

       for ( i = 0; i < topn_val; i++ )
         tmp_cnts[i] = 0;

       /* Find the largest topn_val values from the group of
          2*topn_val values and put them into tmp_cnts. */

       for ( i = 0; i < 2 * topn_val; i += 2 ) 
         {
           p = 0;
           for ( j = 0; j < topn_val; j++ ) 
             {
               if ( !p || tmp_cnts[j] < *p ) 
                  p = &tmp_cnts[j];
             }
            if ( value_array[i + 1] > *p )
              *p = value_array[i + 1];
         }

       minv = tmp_cnts[0];
       for ( j = 1; j < topn_val; j++ )
         {
           if (tmp_cnts[j] < minv)
             minv = tmp_cnts[j];
         }
       /* Zero out low value entries  */
       for ( i = 0; i < 2 * topn_val; i += 2 )
         {
           if (value_array[i + 1] < minv) 
             {
               value_array[i] = 0;
               value_array[i + 1] = 0;
             }
         }
     }
}
#endif

#ifdef L_gcov_one_value_profiler
void
__gcov_one_value_profiler (gcov_type *counters, gcov_type value)
{
  __gcov_one_value_profiler_body (counters, value);
}
#endif

#ifdef L_gcov_indirect_call_profiler

/* By default, the C++ compiler will use function addresses in the
   vtable entries.  Setting TARGET_VTABLE_USES_DESCRIPTORS to nonzero
   tells the compiler to use function descriptors instead.  The value
   of this macro says how many words wide the descriptor is (normally 2),
   but it may be dependent on target flags.  Since we do not have access
   to the target flags here we just check to see if it is set and use
   that to set VTABLE_USES_DESCRIPTORS to 0 or 1.

   It is assumed that the address of a function descriptor may be treated
   as a pointer to a function.  */

#ifdef TARGET_VTABLE_USES_DESCRIPTORS
#define VTABLE_USES_DESCRIPTORS 1
#else
#define VTABLE_USES_DESCRIPTORS 0
#endif

/* Tries to determine the most common value among its inputs. */
void
__gcov_indirect_call_profiler (gcov_type* counter, gcov_type value,
			       void* cur_func, void* callee_func)
{
  /* If the C++ virtual tables contain function descriptors then one
     function may have multiple descriptors and we need to dereference
     the descriptors to see if they point to the same function.  */
  if (cur_func == callee_func
      || (VTABLE_USES_DESCRIPTORS && callee_func
	  && *(void **) cur_func == *(void **) callee_func))
    __gcov_one_value_profiler_body (counter, value);
}
#endif


#ifdef L_gcov_indirect_call_topn_profiler
extern THREAD_PREFIX gcov_type *__gcov_indirect_call_topn_counters ATTRIBUTE_HIDDEN;
extern THREAD_PREFIX void *__gcov_indirect_call_topn_callee ATTRIBUTE_HIDDEN;
#ifdef TARGET_VTABLE_USES_DESCRIPTORS
#define VTABLE_USES_DESCRIPTORS 1
#else
#define VTABLE_USES_DESCRIPTORS 0
#endif
void
__gcov_indirect_call_topn_profiler (void *cur_func,
                                    void *cur_module_gcov_info,
                                    gcov_unsigned_t cur_func_id)
{
  void *callee_func = __gcov_indirect_call_topn_callee;
  gcov_type *counter = __gcov_indirect_call_topn_counters;
  /* If the C++ virtual tables contain function descriptors then one
     function may have multiple descriptors and we need to dereference
     the descriptors to see if they point to the same function.  */
  if (cur_func == callee_func
      || (VTABLE_USES_DESCRIPTORS && callee_func
	  && *(void **) cur_func == *(void **) callee_func))
    {
      gcov_type global_id 
          = ((struct gcov_info *) cur_module_gcov_info)->mod_info->ident;
      global_id = GEN_FUNC_GLOBAL_ID (global_id, cur_func_id);
      __gcov_topn_value_profiler_body (counter, global_id, GCOV_ICALL_TOPN_VAL);
      __gcov_indirect_call_topn_callee = 0;
    }
}

#endif

#ifdef L_gcov_direct_call_profiler
extern THREAD_PREFIX gcov_type *__gcov_direct_call_counters ATTRIBUTE_HIDDEN;
extern THREAD_PREFIX void *__gcov_direct_call_callee ATTRIBUTE_HIDDEN;
/* Direct call profiler. */
void
__gcov_direct_call_profiler (void *cur_func,
			     void *cur_module_gcov_info,
			     gcov_unsigned_t cur_func_id)
{
  if (cur_func == __gcov_direct_call_callee)
    {
      gcov_type global_id 
          = ((struct gcov_info *) cur_module_gcov_info)->mod_info->ident;
      global_id = GEN_FUNC_GLOBAL_ID (global_id, cur_func_id);
      __gcov_direct_call_counters[0] = global_id;
      __gcov_direct_call_counters[1]++;
      __gcov_direct_call_callee = 0;
    }
}
#endif


#ifdef L_gcov_average_profiler
/* Increase corresponding COUNTER by VALUE.  FIXME: Perhaps we want
   to saturate up.  */

void
__gcov_average_profiler (gcov_type *counters, gcov_type value)
{
  counters[0] += value;
  counters[1] ++;
}
#endif

#ifdef L_gcov_ior_profiler
/* Increase corresponding COUNTER by VALUE.  FIXME: Perhaps we want
   to saturate up.  */

void
__gcov_ior_profiler (gcov_type *counters, gcov_type value)
{
  *counters |= value;
}
#endif

#ifdef L_gcov_fork
/* A wrapper for the fork function.  Flushes the accumulated profiling data, so
   that they are not counted twice.  */

pid_t
__gcov_fork (void)
{
  __gcov_flush ();
  return fork ();
}
#endif

#ifdef L_gcov_execl
/* A wrapper for the execl function.  Flushes the accumulated profiling data, so
   that they are not lost.  */

int
__gcov_execl (const char *path, char *arg, ...)
{
  va_list ap, aq;
  unsigned i, length;
  char **args;

  __gcov_flush ();

  va_start (ap, arg);
  va_copy (aq, ap);

  length = 2;
  while (va_arg (ap, char *))
    length++;
  va_end (ap);

  args = (char **) alloca (length * sizeof (void *));
  args[0] = arg;
  for (i = 1; i < length; i++)
    args[i] = va_arg (aq, char *);
  va_end (aq);

  return execv (path, args);
}
#endif

#ifdef L_gcov_execlp
/* A wrapper for the execlp function.  Flushes the accumulated profiling data, so
   that they are not lost.  */

int
__gcov_execlp (const char *path, char *arg, ...)
{
  va_list ap, aq;
  unsigned i, length;
  char **args;

  __gcov_flush ();

  va_start (ap, arg);
  va_copy (aq, ap);

  length = 2;
  while (va_arg (ap, char *))
    length++;
  va_end (ap);

  args = (char **) alloca (length * sizeof (void *));
  args[0] = arg;
  for (i = 1; i < length; i++)
    args[i] = va_arg (aq, char *);
  va_end (aq);

  return execvp (path, args);
}
#endif

#ifdef L_gcov_execle
/* A wrapper for the execle function.  Flushes the accumulated profiling data, so
   that they are not lost.  */

int
__gcov_execle (const char *path, char *arg, ...)
{
  va_list ap, aq;
  unsigned i, length;
  char **args;
  char **envp;

  __gcov_flush ();

  va_start (ap, arg);
  va_copy (aq, ap);

  length = 2;
  while (va_arg (ap, char *))
    length++;
  va_end (ap);

  args = (char **) alloca (length * sizeof (void *));
  args[0] = arg;
  for (i = 1; i < length; i++)
    args[i] = va_arg (aq, char *);
  envp = va_arg (aq, char **);
  va_end (aq);

  return execve (path, args, envp);
}
#endif

#ifdef L_gcov_execv
/* A wrapper for the execv function.  Flushes the accumulated profiling data, so
   that they are not lost.  */

int
__gcov_execv (const char *path, char *const argv[])
{
  __gcov_flush ();
  return execv (path, argv);
}
#endif

#ifdef L_gcov_execvp
/* A wrapper for the execvp function.  Flushes the accumulated profiling data, so
   that they are not lost.  */

int
__gcov_execvp (const char *path, char *const argv[])
{
  __gcov_flush ();
  return execvp (path, argv);
}
#endif

#ifdef L_gcov_execve
/* A wrapper for the execve function.  Flushes the accumulated profiling data, so
   that they are not lost.  */

int
__gcov_execve (const char *path, char *const argv[], char *const envp[])
{
  __gcov_flush ();
  return execve (path, argv, envp);
}
#endif

#ifdef __GCOV_KERNEL__
/*
 * Provide different implementation for the following functions:
 *   __gcov_init
 *   __gcov_exit
 *
 * Provide the following dummy merge functions:
 *   __gcov_merge_add
 *   __gcov_merge_single
 *   __gcov_merge_delta
 *   __gcov_merge_ior
 *   __gcov_merge_icall_topn
 *   __gcov_merge_dc
 *   __gcov_merge_reusedist
 *
 * Reuse the following functions:
 *   __gcov_interval_profiler()
 *   __gcov_pow2_profiler()
 *   __gcov_average_profiler()
 *   __gcov_ior_profiler()
 *   __gcov_one_value_profiler()
 *   __gcov_indirect_call_profiler()
 *     |-> __gcov_one_value_profiler_body()
 *
 * For LIPO: (TBD)
 *  Change slightly for the following functions:
 *   __gcov_merge_icall_topn
 *   __gcov_merge_dc
 *
 *  Reuse the following functions:
 *   __gcov_direct_call_profiler()
 *   __gcov_indirect_call_topn_profiler()
 *     |-> __gcov_topn_value_profiler_body()
 *
 */

/* Current virual gcda file. This is for kernel use only.  */
gcov_kernel_vfile *gcov_current_file;

/* Set current virutal gcda file. It needs to be set before dumping
   profile data.  */

void
gcov_set_vfile (gcov_kernel_vfile *file)
{
  gcov_current_file = file;
}

/* Dump one entry in the gcov_info list (for one object) in kernel.  */

void
gcov_kernel_dump_one_gcov (struct gcov_info *info)
{
  gcc_assert (gcov_current_file);

  gcov_exit_init ();

  gcov_dump_one_gcov (info);
}

#define DUMMY_FUNC(func) \
void func (gcov_type *counters  __attribute__ ((unused)), \
           unsigned n_counters __attribute__ ((unused))) {}

DUMMY_FUNC (__gcov_merge_add)
EXPORT_SYMBOL (__gcov_merge_add);

DUMMY_FUNC (__gcov_merge_single)
EXPORT_SYMBOL (__gcov_merge_single);

DUMMY_FUNC (__gcov_merge_delta)
EXPORT_SYMBOL (__gcov_merge_delta);

DUMMY_FUNC(__gcov_merge_ior)
EXPORT_SYMBOL (__gcov_merge_ior);

DUMMY_FUNC (__gcov_merge_icall_topn)
EXPORT_SYMBOL (__gcov_merge_icall_topn);

DUMMY_FUNC (__gcov_merge_dc)
EXPORT_SYMBOL (__gcov_merge_dc);

DUMMY_FUNC (__gcov_merge_reusedist)
EXPORT_SYMBOL (__gcov_merge_reusedist);

EXPORT_SYMBOL (__gcov_average_profiler);
EXPORT_SYMBOL (__gcov_indirect_call_profiler);
EXPORT_SYMBOL (__gcov_interval_profiler);
EXPORT_SYMBOL (__gcov_ior_profiler);
EXPORT_SYMBOL (__gcov_one_value_profiler);
EXPORT_SYMBOL (__gcov_pow2_profiler);

#endif /* __GCOV_KERNEL__ */

#endif /* inhibit_libc */
