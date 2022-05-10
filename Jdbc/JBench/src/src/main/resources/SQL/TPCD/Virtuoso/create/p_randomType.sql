--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
create procedure tpcd..randomType(in n integer) {
	
	declare syl1, syl2, syl3 integer;
	
	syl1 := vector('STANDARD', 'SMALL', 'MEDIUM', 'LARGE', 'ECONOMY', 'PROMO');
	syl2 := vector('ANODIZED', 'BURNISHED', 'PLATED', 'POLISHED', 'BRUSHED');
	syl3 := vector('TIN', 'NICKEL', 'BRASS', 'STEEL', 'COPPER');
		  
	return concat(aref(syl1, tpcd..randomNumber(0, 4)), ' ', aref(syl2, tpcd..randomNumber(0, 4)), ' ', aref(syl3, tpcd..randomNumber(0, 4)));
}
